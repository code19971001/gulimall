package com.it.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.it.common.to.mq.OrderTo;
import com.it.common.to.mq.SeckillOrderTo;
import com.it.common.utils.PageUtils;
import com.it.common.utils.Query;
import com.it.common.utils.R;
import com.it.common.vo.MemberResponseVo;
import com.it.gulimall.order.constant.OrderConstant;
import com.it.gulimall.order.dao.OrderDao;
import com.it.gulimall.order.entity.OrderEntity;
import com.it.gulimall.order.entity.OrderItemEntity;
import com.it.gulimall.order.entity.PaymentInfoEntity;
import com.it.gulimall.order.enume.AlipayStatusEnum;
import com.it.gulimall.order.enume.OrderStatusEnum;
import com.it.gulimall.order.feign.CartFeignService;
import com.it.gulimall.order.feign.MemberFeignService;
import com.it.gulimall.order.feign.ProductFeignService;
import com.it.gulimall.order.feign.WareFeignService;
import com.it.gulimall.order.interceptor.LoginInterceptor;
import com.it.gulimall.order.service.OrderItemService;
import com.it.gulimall.order.service.OrderService;
import com.it.gulimall.order.service.PaymentInfoService;
import com.it.gulimall.order.to.OrderCreateTo;
import com.it.gulimall.order.vo.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    private ThreadLocal<OrderSubmitVo> threadLocal = new ThreadLocal<>();

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    OrderItemService orderItemService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    PaymentInfoService paymentInfoService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    /**
     * 1、修改为异步调用：
     * feign异步调用会丢失上下文，feign拦截器实现上下文共享是通过ThreadLocal来实现的，一旦我们进行异步调用，就会丢失请求的上下文信息。
     * 解决方式：异步调用线程共享主线程的请求信息。
     * 2、使用防止重复令牌
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberResponseVo loginUser = LoginInterceptor.threadLocal.get();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture<Void> memberTask = CompletableFuture.runAsync(() -> {
            //1.远程查询所有的地址列表
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberReceiveAddressVo> receiveAddress = memberFeignService.getMemberReceiveAddress(loginUser.getId());
            orderConfirmVo.setAddress(receiveAddress);
        }, threadPoolExecutor);
        CompletableFuture<Void> orderTask = CompletableFuture.runAsync(() -> {
            //2.购物车选中的购物项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> curUserCartItems = cartFeignService.getCurUserCartItems();
            orderConfirmVo.setOrderItems(curUserCartItems);
        }, threadPoolExecutor).thenRunAsync(() -> {
            List<Long> skuIds = orderConfirmVo.getOrderItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skuStock = wareFeignService.hasStock(skuIds);
            Map<Long, Boolean> stockMap;
            if (skuStock.getCode() == 0) {
                List<SkuStockVo> skuStockData = skuStock.getData(new TypeReference<List<SkuStockVo>>() {
                });
                if (skuStockData != null) {
                    stockMap = skuStockData.stream().collect(Collectors.toMap(SkuStockVo::getSkuId, SkuStockVo::getHasStock));
                } else {
                    stockMap = skuIds.stream().collect(Collectors.toMap(skuId -> skuId, skuStockVo -> false));
                }
            } else {
                stockMap = skuIds.stream().collect(Collectors.toMap(skuId -> skuId, skuStockVo -> false));
            }
            orderConfirmVo.setSkuStocks(stockMap);
        });
        Integer integration = loginUser.getIntegration();
        orderConfirmVo.setIntegration(integration);
        //创建防止重复令牌
        String token = UUID.randomUUID().toString().replace("-", "");
        //1.存放令牌到redis
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId(), token, 30, TimeUnit.MINUTES);
        //2.存放令牌给页面
        orderConfirmVo.setOrderToken(token);
        CompletableFuture.allOf(memberTask, orderTask).get();
        return orderConfirmVo;
    }

    /**
     * 分布式事务：服务之间调用，一旦某一次调用失败就全体回滚。
     * 1）如果直接使用异常机制有什么缺点？--本地事务
     * -->远程服务假失败：假设库存服务，锁库存成功，实际事务已经提交，但是返回的时候连接超时了，导致order方法出现异常，order回滚，但是远程的库存服务是无法进行回滚的。
     * -->远程服务执行完成，下面的其他方法出现异常，已经执行的远程服务肯定是无法回滚的。
     * -->分布式事务的最大问题是网络问题。无法区分是真的失败还是因为网络抖动而失败。
     * 2）事务的传播行为
     * -->坑：同一个对象内事务方法互调默认失效。
     * 原因：绕过了代理对象.
     * 解决方式：使用代理对象来调用事务方法。
     * -->引入starter-aop,使用aspectj
     * -->开启aspectj的动态代理功能，使用@EnableAspectJAutoProxy(exposeProxy=true),即使没有接口也可以创建动态代理.
     * -->方法调用的时候：(OrderServiceImpl)AopContext.currentProxy();然后进行方法的调用。
     * <p>
     * 为了保证高并发不可以使用AT模式，也就是二阶提交模式，为了保证高并发，库存服务可以自己回滚，可以发消息给库存服务，库存模块也需要可以实现库存解锁功能。
     */
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        threadLocal.set(submitVo);
        SubmitOrderResponseVo responseVo = new SubmitOrderResponseVo();
        //令牌验证和删除：需要保证原子性，使用lua脚本:0代表脚本校验失败。1代表删除成功
        String orderToken = submitVo.getOrderToken();
        MemberResponseVo loginUser = LoginInterceptor.threadLocal.get();
        String userTokenRedisKey = OrderConstant.USER_ORDER_TOKEN_PREFIX + loginUser.getId();
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        Long executeResult = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(userTokenRedisKey), orderToken);
        if (executeResult == 0) {
            //令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }
        //2.创建订单
        OrderCreateTo order = createOrder();
        //验证价格
        BigDecimal payAmount = order.getOrder().getPayAmount();
        BigDecimal payPrice = submitVo.getPayPrice();
        if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
            //金额对比成功
            //3.保存订单信息
            saveOrder(order);
            //4.锁定库存,一旦存在异常就抛出异常，回滚订单数据。订单号，所有的订单项(skuId,skuName,num)
            WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
            wareSkuLockVo.setOrderSn(order.getOrder().getOrderSn());
            wareSkuLockVo.setOrderItems(order.getOrderItems().stream().map(item -> {
                OrderItemVo orderItemVo = new OrderItemVo();
                orderItemVo.setSkuId(item.getSkuId());
                orderItemVo.setCount(item.getSkuQuantity());
                return orderItemVo;
            }).collect(Collectors.toList()));
            R r = wareFeignService.orderLockStock(wareSkuLockVo);
            if (r.getCode() == 0) {
                //锁定成功
                responseVo.setCode(0);
                responseVo.setOrderEntity(order.getOrder());
                //订单创建成功,发送消息给RabbitMq
                rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", order.getOrder());
                return responseVo;
            } else {
                //锁定失败
                responseVo.setCode(3);
                return responseVo;
            }
        } else {
            //验证价格失败
            responseVo.setCode(2);
            return responseVo;
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return this.baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
    }

    /**
     * 关闭订单之前，先去数据库中查询订单是否支付成功
     */
    @Override
    public void closeOrder(OrderEntity orderEntity) {
        OrderEntity orderId = this.getById(orderEntity.getId());
        //如果依旧是待付款状态，就需要将订单关闭
        if (orderId.getStatus() == OrderStatusEnum.CREATE_NEW.getCode()) {
            OrderEntity entity = new OrderEntity();
            entity.setId(orderEntity.getId());
            entity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(entity);
            OrderEntity order = this.getById(entity.getId());
            //发送给rabbitmq一个消息，订单关闭了
            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(order, orderTo);
            try {
                //需要保证消息一定会发送出去，每一个发送出去的消息做好消息记录，在数据库中创建好一张表，用于存储所有发送出去的消息，只要发送失败，定期扫描数据库再次发送消息。
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo);
            } catch (Exception e) {
                //todo:出现问题，将没有发送成功的消息进行重试发送，做好日志记录
            }

        }
    }

    /**
     * 根据订单号，配置PayVo.
     */
    @Override
    public PayVo getOrderPay(String orderSn) {
        OrderEntity orderEntity = this.getOrderByOrderSn(orderSn);
        List<OrderItemEntity> orderItems = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", orderSn));
        PayVo payVo = new PayVo();
        //注意支付宝支付的要求小数点后两位,数据库中精确度更高，因此我们需要进行格式化。
        payVo.setTotal_amount(orderEntity.getTotalAmount().setScale(2, BigDecimal.ROUND_UP).toString());
        payVo.setOut_trade_no(orderSn);
        //设置支付的标题和body
        payVo.setSubject(orderItems.get(0).getSkuName());
        payVo.setBody(orderItems.get(0).getSkuAttrsVals());
        return payVo;
    }

    @Override
    public PageUtils queryPageWithItem(Map<String, Object> params) {
        MemberResponseVo memberResponseVo = LoginInterceptor.threadLocal.get();

        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>().eq("member_id", memberResponseVo.getId()).orderByDesc("id")
        );
        List<OrderEntity> orderEntities = page.getRecords().stream().map(order -> {
            List<OrderItemEntity> orderItems = orderItemService.list(new QueryWrapper<OrderItemEntity>().eq("order_sn", order.getOrderSn()));
            order.setOrderItems(orderItems);
            return order;
        }).collect(Collectors.toList());
        page.setRecords(orderEntities);
        return new PageUtils(page);
    }

    @Override
    public String handlePayResult(PayAsyncVo payAsyncVo) {
        //1.保存交易流水
        PaymentInfoEntity paymentInfo = new PaymentInfoEntity();
        paymentInfo.setAlipayTradeNo(payAsyncVo.getTrade_no());
        paymentInfo.setOrderSn(payAsyncVo.getOut_trade_no());
        paymentInfo.setPaymentStatus(payAsyncVo.getTrade_status());
        paymentInfo.setCallbackTime(payAsyncVo.getNotify_time());
        paymentInfoService.save(paymentInfo);
        //2.修改订单的状态信息
        if (payAsyncVo.getTrade_status().equalsIgnoreCase(AlipayStatusEnum.TRADE_SUCCESS.getMsg()) || payAsyncVo.getTrade_status().equalsIgnoreCase(AlipayStatusEnum.TRADE_FINISHED.getMsg())) {
            //支付成功创建，修改订单的状态
            String orderSn = payAsyncVo.getOut_trade_no();
            this.baseMapper.updateOrderStatus(orderSn, OrderStatusEnum.PAYED.getCode());
        }
        return "success";
    }

    /**
     * todo:保存其他的信息
     */
    @Override
    public void createSeckillOrder(SeckillOrderTo seckillOrderTo) {
        //保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(seckillOrderTo.getOrderSn());
        orderEntity.setMemberId(seckillOrderTo.getMemberId());
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        BigDecimal multiply = seckillOrderTo.getSeckillPrice().multiply(seckillOrderTo.getNum());
        orderEntity.setPayAmount(multiply);
        orderEntity.setCreateTime(new Date());
        this.baseMapper.insert(orderEntity);
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderSn(seckillOrderTo.getOrderSn());
        orderItem.setRealAmount(multiply);
        orderItem.setSkuQuantity(seckillOrderTo.getNum().intValue());
        //保存当前sku的详细信息进行设置
        orderItemService.save(orderItem);
    }

    /**
     * 保存订单数据
     */
    private void saveOrder(OrderCreateTo order) {
        OrderEntity orderEntity = order.getOrder();
        orderEntity.setModifyTime(new Date());
        this.baseMapper.insert(orderEntity);
        List<OrderItemEntity> orderItems = order.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    /**
     * 创建订单：
     * 1）创建订单号。
     * 2）设置订单的收货人信息
     * 3）获取订单项信息
     */
    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        //构建订单
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(orderSn);
        //构建所有订单项信息
        List<OrderItemEntity> orderItems = buildOrderItems(orderSn);
        orderCreateTo.setOrder(order);
        orderCreateTo.setOrderItems(orderItems);
        //计算价格，积分相关
        computerPrice(order, orderItems);
        return orderCreateTo;
    }

    /**
     * todo:计算价格
     * 1）订单的总额。
     * 2）积分信息
     */
    private void computerPrice(OrderEntity order, List<OrderItemEntity> orderItems) {
        BigDecimal totalAmount = new BigDecimal("0.0");
        BigDecimal couponAmount = new BigDecimal("0.0");
        BigDecimal integrationAmount = new BigDecimal("0.0");
        BigDecimal promotionAmount = new BigDecimal("0.0");
        BigDecimal giftIntegration = new BigDecimal("0.0");
        BigDecimal giftGrowth = new BigDecimal("0.0");
        for (OrderItemEntity item : orderItems) {
            couponAmount = couponAmount.add(item.getCouponAmount());
            integrationAmount = integrationAmount.add(item.getIntegrationAmount());
            promotionAmount = promotionAmount.add(item.getPromotionAmount());
            totalAmount = totalAmount.add(item.getRealAmount());
            giftIntegration = giftIntegration.add(new BigDecimal(item.getGiftIntegration()));
            giftGrowth = giftGrowth.add(new BigDecimal(item.getGiftGrowth()));
        }
        //叠加每一个订单项的总额
        order.setTotalAmount(totalAmount);
        order.setCouponAmount(couponAmount);
        order.setIntegrationAmount(integrationAmount);
        order.setPayAmount(totalAmount.add(order.getFreightAmount()));
        //设置订单的状态信息
        order.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        order.setAutoConfirmDay(OrderConstant.AUTO_CONFIRM_DAY);
        //设置积分信息
        order.setIntegration(giftIntegration.intValue());
        order.setGrowth(giftGrowth.intValue());

    }

    /**
     * 构建订单吗
     */
    private OrderEntity buildOrder(String orderSn) {
        MemberResponseVo memberResponseVo = LoginInterceptor.threadLocal.get();
        OrderSubmitVo submitVo = threadLocal.get();
        OrderEntity order = new OrderEntity();
        order.setMemberId(memberResponseVo.getId());
        order.setOrderSn(orderSn);
        //获取收货地址信息
        R ware = wareFeignService.getFare(submitVo.getAddrId());
        FireRespVo fireRespVo = ware.getData(new TypeReference<FireRespVo>() {
        });
        //设置运费
        order.setFreightAmount(fireRespVo.getFire());
        //设置收货人信息
        order.setReceiverName(fireRespVo.getMemberReceiveAddress().getName());
        order.setReceiverPhone(fireRespVo.getMemberReceiveAddress().getPhone());
        order.setReceiverPostCode(fireRespVo.getMemberReceiveAddress().getPostCode());
        order.setReceiverProvince(fireRespVo.getMemberReceiveAddress().getProvince());
        order.setReceiverCity(fireRespVo.getMemberReceiveAddress().getCity());
        order.setReceiverRegion(fireRespVo.getMemberReceiveAddress().getRegion());
        order.setReceiverDetailAddress(fireRespVo.getMemberReceiveAddress().getDetailAddress());
        return order;
    }


    /**
     * 构建所有的订单项信息
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //这里十分的重要，最后确定每个订单项的价格
        List<OrderItemVo> curUserCartItems = cartFeignService.getCurUserCartItems();
        if (curUserCartItems != null && !curUserCartItems.isEmpty()) {
            //构建所有的订单项数据
            return curUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItem = buildOrderItem(cartItem);
                orderItem.setOrderSn(orderSn);
                return orderItem;
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * todo:确认价格是最新的价格
     * 1、订单信息。订单号
     * 2、商品的spu信息。
     * 3、商品的sku信息
     * 4、商品的优惠信息[暂时不做]
     * 5、积分信息
     * 6.封装订单项的金额信息
     */
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItem = new OrderItemEntity();
        //设置spu信息
        R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());
        SpuInfoVo spuInfo = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {
        });
        orderItem.setSpuId(spuInfo.getId());
        orderItem.setSpuName(spuInfo.getSpuName());
        //todo:将品牌的id改为品牌名字;封装分类的名字
        orderItem.setSpuBrand(spuInfo.getBrandId().toString());
        orderItem.setCategoryId(spuInfo.getCatalogId());
        //设置sku信息
        orderItem.setSkuId(cartItem.getSkuId());
        orderItem.setSkuName(cartItem.getTitle());
        orderItem.setSkuPic(cartItem.getImage());
        String skuAttrs = StringUtils.collectionToDelimitedString(cartItem.getSkuAttr(), ";");
        orderItem.setSkuAttrsVals(skuAttrs);
        orderItem.setSkuPrice(cartItem.getPrice());
        orderItem.setSkuQuantity(cartItem.getCount());
        //设置积分和成长值信息
        orderItem.setGiftGrowth(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        orderItem.setGiftIntegration(cartItem.getPrice().multiply(new BigDecimal(cartItem.getCount())).intValue());
        orderItem.setIntegrationAmount(new BigDecimal(orderItem.getGiftGrowth() + orderItem.getGiftIntegration()));
        //设置订单项的价格信息：实际金额等于总额减去所有的优惠
        orderItem.setPromotionAmount(new BigDecimal("0.0"));
        orderItem.setCouponAmount(new BigDecimal("0.0"));
        BigDecimal origPrice = cartItem.getPrice().multiply(new BigDecimal(orderItem.getSkuQuantity().toString()));
        orderItem.setRealAmount(origPrice.subtract(orderItem.getPromotionAmount()).subtract(orderItem.getCouponAmount()));
        return orderItem;
    }


}