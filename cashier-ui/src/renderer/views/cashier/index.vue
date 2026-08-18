<template>
   <div id="app" class="app-container">
     <el-drawer
       :withHeader="false"
       :visible.sync="isOpen"
       direction="ttb"
       size="100%"
       :before-close="handleClose"
       :show-close="false">
       <div class="main">
         <div class="top-nav">欢迎使用{{ systemName }}</div>
         <div class="left-side">
           <div class="logo">
             <i class="logout el-icon-d-arrow-left" @click="logout"></i>
             <div class="title">收银台</div>
             <div class="store" v-if="storeInfo">({{ storeInfo.name }})</div>
             <div class="account">您好，{{ accountInfo.realName ? accountInfo.realName : accountInfo.accountName }}！</div>
           </div>
           <div class="cate">
             <ul class="nav">
               <li class="nav-item" v-for="menu in menuList">
                 <a :class="'nav-link' + (activeMenu == menu.key ? ' active' : '')" href="javascript:;" @click="switchMenu(menu.key)">
                    <img class="cate-logo" :src="menu.logo">
                    <span>{{ menu.name }}</span>
                 </a>
               </li>
             </ul>
           </div>
         </div>
         <div class="cart-container" v-if="activeMenu == 'cashier'">
           <div class="title">
             <div class="logo">
               <img class="avatar" v-if="!memberInfo || !memberInfo.avatar" src="@/assets/images/avatar.png" />
               <img class="avatar" v-else :src="memberInfo.avatar"/>
             </div>
             <div class="member-info">
               <span class="name" v-if="memberInfo">{{ memberInfo.mobile ? memberInfo.mobile : memberInfo.name }}</span>
               <span class="none" v-if="!memberInfo">当前为游客</span>
               <el-button size="mini" class="switch" type="danger" icon="el-icon-refresh" @click="switchMember()">关联会员</el-button>
             </div>
           </div>
           <div class="carts">
             <div>
               <div class="tab">
                 <div class="cart-list" v-if="cartList.length > 0">
                   <div class="cart-item" v-for="cartInfo in cartList">
                     <img class="image" :src="cartInfo.logo"/>
                     <div class="info">
                       <div class="name">{{ cartInfo.name }}</div>
                       <div class="spec" v-if="cartInfo.specList && cartInfo.specList.length > 0">
                         <span class="item" v-for="spec in cartInfo.specList" :title="spec.value">{{ spec.value }}</span>
                       </div>
                       <div class="num"><el-input-number class="input" @change="changeBuyNum(cartInfo)" v-model="cartInfo.buyNum" :step="1" :min="0.01" :max="1000"/></div>
                     </div>
                     <div class="option">
                       <div class="remove el-icon-delete" @click="removeFromCart(cartInfo.cartId)"></div>
                       <div class="total">￥{{ (cartInfo.price * cartInfo.buyNum).toFixed(2) }}</div>
                     </div>
                   </div>
                 </div>
                 <div class="empty" v-if="cartList.length < 1">
                     <el-empty description="暂无结算商品" :image-size="40"></el-empty>
                 </div>
               </div>
             </div>
           </div>
           <div class="footer">
             <div class="number">
               <div class="total-num">总件数：<b class="num">{{ cartTotalNum.toFixed(2) }}</b></div>
               <div class="total-price">总金额：<b class="num">￥{{ cartTotalPrice ? cartTotalPrice.toFixed(2) : '0.00' }}</b></div>
             </div>
             <div class="options">
               <div class="cash" @click="hangUp()">挂单 / 取单</div>
               <div class="submit" v-if="cartTotalNum > 0" @click="doSettlement()">提交结算</div>
               <div class="submit" v-if="cartTotalNum == 0" @click="doCashier()">无商品收款</div>
             </div>
           </div>
         </div>
         <div class="main-list" v-if="activeMenu == 'cashier'">
            <div class="title">
              <el-form class="search-form" ref="searchForm" :inline="true" :model="searchForm">
                <el-form-item class="form-item" label="" prop="keyword">
                  <el-input v-model="searchForm.keyword" prefix-icon="el-icon-full-screen" @keyup.enter.native="doQueryGoods" class="input-item" placeholder="请输入商品关键字：商品名称、条码、商品ID..." clearable maxlength="100" />
                </el-form-item>
                <el-button class="search-goods" @click="doQueryGoods()" icon="el-icon-search">查询商品</el-button>
                <el-form-item class="setting">
                  <span :class="goodsMode == 'small' ? 'item active' : 'item'" @click="switchGoodMode('small')"><svg t="1741317847001" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="6842" width="16" height="16"><path d="M263.104 760.896V960H64v-199.104h199.104z m348.48 0V960H412.416v-199.104h199.168z m348.416 0V960h-199.104v-199.104H960zM263.104 412.416v199.168H64V412.416h199.104z m348.48 0v199.168H412.416V412.416h199.168z m348.416 0v199.168h-199.104V412.416H960zM263.104 64v199.104H64V64h199.104z m348.48 0v199.104H412.416V64h199.168zM960 64v199.104h-199.104V64H960z" p-id="6843"></path></svg><span class="text">小图</span></span>
                  <span :class="goodsMode == 'big' ? 'item active' : 'item'" @click="switchGoodMode('big')"><svg t="1741318028632" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="2278" width="16" height="16"><path d="M934.4 934.4V633.6H633.6v300.8h300.8m-544 0V633.6H89.6v300.8h300.8m544-544V89.6H633.6v300.8h300.8m-544 0V89.6H89.6v300.8h300.8M960 1024H608a64 64 0 0 1-64-64V608a64 64 0 0 1 64-64H960a64 64 0 0 1 64 64V960a64 64 0 0 1-64 64z m-544 0H64a64 64 0 0 1-64-64V608a64 64 0 0 1 64-64h352a64 64 0 0 1 64 64V960a64 64 0 0 1-64 64zM960 480H608a64 64 0 0 1-64-64V64a64 64 0 0 1 64-64H960a64 64 0 0 1 64 64v352a64 64 0 0 1-64 64z m-544 0H64a64 64 0 0 1-64-64V64a64 64 0 0 1 64-64h352a64 64 0 0 1 64 64v352a64 64 0 0 1-64 64z" fill="#333333" p-id="2279"></path></svg><span class="text">大图</span></span>
                </el-form-item>
              </el-form>
              <el-tabs class="tab-box" type="card" v-model="navTab" @tab-click="switchTab">
                <el-tab-pane label="全部" name="0"></el-tab-pane>
                <el-tab-pane v-for="tab in tabList" :label="tab.name" :key="tab.id+''" :name="tab.id+''"></el-tab-pane>
              </el-tabs>
            </div>
            <div :class="'goods-list-' + goodsMode">
              <div class="goods-item" v-for="goodsInfo in goodsList">
                  <div class="item" @click="clickGoods(goodsInfo)">
                    <img class="image" lazy :src="imagePath + goodsInfo.logo">
                    <div class="goods-name">{{ goodsInfo.name }}</div>
                    <div class="goods-price">￥{{ goodsInfo.price }}</div>
                  </div>
              </div>

              <pagination
                  v-show="totalGoods > 0"
                  :total="totalGoods"
                  class="pagination"
                  :page.sync="page"
                  :limit.sync="pageSize"
                  @pagination="initCashier"
              />

              <el-empty v-if="goodsList.length == 0" description="暂无商品..."></el-empty>
            </div>
         </div>

         <!-- 订单列表组件 start-->
         <orderList v-if="activeMenu == 'order'" @doPayOrder="doPayOrder"></orderList>
         <!-- 订单列表组件 end-->

         <!-- 会员列表组件 start-->
         <memberList v-if="activeMenu == 'member'" @upgrade-member="handleMemberUpgrade"></memberList>
         <!-- 会员列表组件 end-->

         <!-- 会员卡券组件 start-->
         <userCoupon v-if="activeMenu == 'userCoupon' && !confirmCoupon" @doConfirmCoupon="doConfirmCoupon"></userCoupon>
         <!-- 会员卡券组件 end-->

         <!-- 卡券核销组件 start-->
         <couponConfirm v-if="confirmCoupon" :couponCode="couponCode" @doUserCoupon="doUserCoupon"></couponConfirm>
         <!-- 卡券核销组件 end-->
       </div>
     </el-drawer>

     <!-- 规格详情 start-->
     <el-dialog title="选择商品规格" :visible.sync="openGoodsDialog" class="common-dialog" append-to-body>
       <div class="goods-info">
         <div class="name">{{ goodsInfo.name }}</div>
         <div class="price">￥{{ goodsInfo.price }}</div>
         <div class="num"><el-input-number class="input" v-model="goodsNum" :step="1" :min="0.01" :max="1000"/></div>
         <div class="spec-list" v-if="goodsInfo.isSingleSpec == 'N'">
             <div class="spec-item" v-for="specInfo in goodsInfo.specList">
                <div class="spec-name">{{ specInfo.name }}</div>
                <div class="values">
                  <span v-for="value in specInfo.child" :class="goodsSpecIds.includes(value.id) ? 'value active' : 'value'" @click="selectGoodsSpec(value.id)">{{ value.name }}</span>
                  <div class="clearfix"></div>
                </div>
             </div>
         </div>
       </div>
       <div slot="footer" class="dialog-footer">
         <el-button type="primary" class="main-button" @click="addToCart()">加入结算</el-button>
         <el-button @click="closeGoodsDialog()">取 消</el-button>
       </div>
     </el-dialog>
     <!-- 规格详情 end-->

     <!--关联会员对话框 start-->
     <switchMemberDialog :show-dialog="openSwitchMemberDialog" @doSwitchMember="doSwitchMember"/>
     <!--关联会员对话框 end-->

     <!--关联员工对话框 start-->
     <bindStaffDialog :show-dialog="openBindStaffDialog" @doBindStaff="doBindStaff" @closeDialog="closeDialog"/>
     <!--关联员工对话框 end-->

     <!--结算对话框 start-->
     <settlementDialog :show-dialog="openSettlementDialog" :memberInfo="memberInfo" :staffInfo="staffInfo" :totalPrice="cartTotalPrice" :remarks="cartRemark" :orderInfo="orderInfo" :couponList="couponList" @submit="submitSettlement" @switchMember="switchMember" @bindStaff="bindStaff" @closeDialog="closeDialog"/>
     <!--结算对话框 end-->

     <!--扫码付款对话框 start-->
     <scanPayCodeDialog ref="scanPayCodeDialog" :show-dialog="openScanPayCodeDialog" :memberInfo="memberInfo" :orderId="orderId" :payType="payType" :payAmount="payAmount" @closeDialog="closeDialog" @showPayResult="showPayResult"/>
     <!--扫码付款对话框 end-->

     <!--支付结果对话框 start-->
     <payResultDialog :show-dialog="openPayResultDialog" :payResult="payResult" @showOrderPrint="showOrderPrint" @closeDialog="closeDialog"/>
     <!--支付结果对话框 end-->

     <!--打印订单对话框 start-->
     <orderPrintDialog :show-dialog="openOrderPrintDialog" :storeInfo="storeInfo" :orderInfo="orderInfo" @closeDialog="closeDialog"/>
     <!--打印订单对话框 end-->

     <!--挂单对话框 start-->
     <hangUpDialog :show-dialog="openHangUpDialog" :memberInfo="memberInfo" :cartList="cartList" @getHangNo="getHangNo" @doHangUp="doHangUp" @closeDialog="closeDialog"/>
     <!--挂单对话框 end-->

     <!-- 无商品收款组件 start-->
     <noGoodsCashier :show-dialog="openNoGoodsCashierDialog" @submit="submitCashier" @closeDialog="closeDialog"></noGoodsCashier>
     <!-- 无商品收款组件 end-->
   </div>
</template>

<script>
import { init, getGoodsInfo, searchGoods, getCartList, saveCart, removeFromCart, submitSettlement, doPay, getMemberInfoById } from "@/api/cashier";
import { getOrderInfo } from "@/api/order";
import { getUserId, setUserId, removeUserId } from '@/utils/auth';
import switchMemberDialog from "./components/switchMemberDialog";
import settlementDialog from "./components/settlementDialog";
import scanPayCodeDialog from "./components/scanPayCodeDialog";
import payResultDialog from "./components/payResultDialog";
import orderPrintDialog from "./components/orderPrintDialog";
import hangUpDialog from "./components/hangUpDialog";
import orderList from "./components/orderList";
import memberList from "./components/memberList";
import userCoupon from "./components/userCoupon";
import couponConfirm from "./components/couponConfirm";
import noGoodsCashier from "./components/noGoodsCashier";
import bindStaffDialog from "./components/bindStaffDialog";
export default {
  name: "Cashier",
  components: {
    switchMemberDialog,
    settlementDialog,
    scanPayCodeDialog,
    payResultDialog,
    orderPrintDialog,
    hangUpDialog,
    orderList,
    memberList,
    userCoupon,
    couponConfirm,
    noGoodsCashier,
    bindStaffDialog
  },
  data() {
    return {
      // 系统名称
      systemName: process.env.userConfig.SYSTEM_NAME,
      // 导航tab
      navTab: '0',
      isOpen: true,
      page: 1,
      pageSize: 15,
      totalGoods: 0,
      openGoodsDialog: false,
      openSwitchMemberDialog: false,
      openBindStaffDialog: false,
      openSettlementDialog: false,
      openNoGoodsCashierDialog: false,
      openScanPayCodeDialog: false,
      openPayResultDialog: false,
      openOrderPrintDialog: false,
      openHangUpDialog: false,
      searchForm: { keyword: '' },
      payResult: { isSuccess: false, payAmount: 0, orderId: 0 },
      goodsForm: {},
      // 左侧菜单
      menuList: [{ name: '收银主页', key: 'cashier', logo: require('../../assets/images/cashier.png') },
                 { name: '订单管理', key: 'order', logo: require('../../assets/images/order.png') },
                 { name: '会员管理', key: 'member', logo: require('../../assets/images/hot.png') },
                 { name: '卡券核销', key: 'userCoupon', logo: require('../../assets/images/life.png') }],
      // 激活菜单
      activeMenu: 'cashier',
      // 导航栏tab
      tabList: [],
      // 当前操作会员
      memberInfo: null,
      // 当前登录用户
      accountInfo: {},
      // 绑定的员工
      staffInfo: null,
      // 当前门店信息
      storeInfo: {},
      // 当前操作商品
      goodsInfo: { num : 1, specList : [], skuList: [] },
      // 当前选择属性
      goodsSpecIds: [],
      // 商品数量
      goodsNum: 1,
      // 当前分类
      cateId: 0,
      // 图片目录
      imagePath: '',
      // 商品分类列表
      cateList: [],
      // 商品列表
      goodsList: [],
      // 购物车列表
      cartList: [],
      // 订单列表
      orderList: [],
      // 总金额
      cartTotalPrice: 0,
      // 购物车备注
      cartRemark: '',
      // 总件数
      cartTotalNum: 0,
      // 支付金额
      payAmount: 0,
      // 当前订单号
      orderId: 0,
      // 支付方式
      payType: '',
      // 当前订单
      orderInfo: {},
      // 可用卡券列表
      couponList: [],
      // 挂单序号
      hangNo: '',
      isSearch: false,
      // 核销卡券码
      couponCode: '',
      // 是否核销卡券
      confirmCoupon: false,
      // 商品模式
      goodsMode: localStorage.getItem("goodsMode") ? localStorage.getItem("goodsMode") : 'small'
    };
  },
  mounted() {
    const app = this;
    // 监听扫码枪按键
    let code = '';
    let lastTime, nextTime; // 上次时间、最新时间
    let lastCode, nextCode; // 上次按键、最新按键
    document.onkeypress = (e) => {
      // 获取按键
      if (window.event) { // IE
          nextCode = e.keyCode;
      } else if (e.which) { // Netscape/Firefox/Opera
          nextCode = e.which;
      }
      // 如果触发了回车事件(扫码结束时间)
      if (nextCode === 13) {
          if (code.length < 3) {
              return;
          }
          if (app.openScanPayCodeDialog == true) {
              app.$refs.scanPayCodeDialog.submit(code);
              return false;
          }
          if (app.openSwitchMemberDialog == true) {
              return false;
          }
          if (app.openSettlementDialog == true) {
              app.$alert("请点击确定收款！");
              return false;
          }
          if (app.activeMenu == 'cashier') {
              app.addToCart(code);
          }
          code = '';
          lastCode = '';
          lastTime = '';
          return true;
      }
      nextTime = new Date().getTime(); // 记录最新时间
      if (!lastTime && !lastCode) { // 如果上次时间和上次按键为空
          code += String(e.key); // 执行叠加操作
      }
      // 如果有上次时间及上次按键
      if (lastCode && lastTime && nextTime - lastTime > 30) {
          code = e.key;
      } else if (lastCode && lastTime) {
          code += String(e.key);
      }
      lastCode = nextCode;
      lastTime = nextTime;
      return true;
    }
  },
  created() {
    this.initCashier();
    this.getCartList();
  },
  methods: {
    // 初始化数据
    initCashier(option) {
      const app = this;
      const userId = getUserId() > 0 ? getUserId() : 0;
      if (option && option.limit) {
          app.pageSize = option.limit;
      }
      init(userId, app.cateId, app.page, app.pageSize).then( response => {
          app.cateList = response.data.cateList;
          app.tabList = response.data.cateList;
          app.goodsList = response.data.goodsList;
          app.imagePath = response.data.imagePath;
          app.storeInfo = response.data.storeInfo;
          app.accountInfo = response.data.accountInfo;
          app.memberInfo = response.data.memberInfo;
          app.totalGoods = response.data.totalGoods;
          app.staffInfo = response.data.staffInfo ? response.data.staffInfo : null;
          app.loading = false;
          app.hangNo = '';
        }
      ).catch((err) => {
          app.loading = false;
          console.log(err.toString());
      });
    },
    // 菜单切换
    switchMenu(menuKey) {
      this.activeMenu = menuKey;
      this.confirmCoupon = false;
      return menuKey;
    },
    // tab切换
    switchTab(el) {
      this.navTab = el.name;
      this.filterCate(this.navTab);
    },
    // 过滤分类商品
    filterCate(cateId) {
       this.cateId = cateId;
       this.initCashier();
    },
    // 购物车列表
    getCartList(cartIds) {
      const app = this;
      if (app.loading) {
          return false;
      }
      app.loading = true;
      app.cartList = [];
      const switchCartIds = cartIds ? cartIds.join(",") : "";
      getCartList({ userId: getUserId(), hangNo: app.hangNo, cartIds: switchCartIds }).then( response => {
           const cartList = response.data.list;
           if (cartList && cartList.length > 0) {
               cartList.forEach(function(item) {
                  const specList = [];
                  if (item.specList && item.specList.length > 0) {
                      item.specList.forEach(function (spec) {
                        specList.push({name: spec.specName, value: spec.specValue});
                      })
                  }
                  const cartInfo = { cartId: item.id, skuId: item.skuId, goodsId: item.goodsInfo.id, name: item.goodsInfo.name, logo: item.goodsInfo.logo, price: item.goodsInfo.price, buyNum: item.num, specList: specList };
                  app.cartList.push(cartInfo);
               })
           }
           app.cartTotalPrice = response.data.payPrice;
           app.cartTotalNum = response.data.totalNum;
           app.couponList = response.data.couponList;
           app.loading = false;
      }).catch((err) => {
           app.loading = false;
           console.log(err.toString());
      });
    },
    // 查询商品
    doQueryGoods() {
      const app = this;
      if (!app.searchForm.keyword) {
          app.initCashier();
          return false;
      }
      app.loading = true;
      searchGoods({ keyword: app.searchForm.keyword }).then( response => {
         app.loading = false;
         if (response.data && response.data.length > 0) {
             app.goodsList = response.data;
         } else {
             app.$alert("抱歉，未查询到商品信息！");
             return false;
         }
      }).catch((err) => {
          app.loading = false;
          console.log(err.toString());
      });
    },
    // 点击商品规格弹框
    clickGoods(goodsInfo) {
      const app = this;
      if (app.loading) {
          return false;
      }
      app.loading = true;
      getGoodsInfo(goodsInfo.id).then( response => {
          app.goodsInfo = response.data.goodsInfo;
          app.goodsInfo.specList = response.data.specList;
          app.goodsInfo.skuList = response.data.skuList;
          app.goodsNum = 1;
          app.goodsInfo.skuNo = goodsInfo.skuNo;
          app.loading = false;
          if (app.goodsInfo.isSingleSpec == 'N' && !goodsInfo.skuNo) {
              app.openGoodsDialog = true;
          } else {
              app.addToCart(false);
          }
      }).catch((err) => {
          app.loading = false;
          console.log(err.toString());
      });
    },
    // 关闭规格弹框
    closeGoodsDialog() {
       this.openGoodsDialog = false;
    },
    // 选择商品属性
    selectGoodsSpec(specId) {
       const app = this;
       let specIds = [];
       app.goodsInfo.specList.forEach(function() {
          specIds.push(0);
       })
       app.goodsInfo.specList.forEach(function(specItem, index) {
          const children = [];
          specItem.child.forEach(function(child) {
              children.push(child.id)
          })
          if (children.includes(specId)) {
              specIds[index] = specId
          } else {
              specIds[index] = app.goodsSpecIds[index] == undefined ? 0 : app.goodsSpecIds[index];
          }
       })
       app.goodsSpecIds = specIds;
    },
    // 加入购物车
    addToCart(skuNo) {
      const app = this;
      app.isSearch = false;
      // 扫码枪扫描商品条码，直接加入购物车
      if (skuNo) {
          searchGoods({ keyword: skuNo }).then( response => {
            app.loading = false;
            if (response.data && response.data.length > 0) {
                let goodsInfo = response.data[0];
                goodsInfo.skuNo = skuNo;
                app.clickGoods(goodsInfo);
                app.isSearch = true;
                return false;
            } else {
                app.$alert("抱歉，未查询到商品信息！");
                return false;
            }
          }).catch((err) => {
              app.loading = false;
              console.log(err.toString());
          });
      }
      if (app.loading || app.isSearch || !app.goodsInfo.id || app.goodsNum <= 0) {
          return false;
      }
      const specIds = app.goodsSpecIds.join('-');
      let skuId = 0;
      app.goodsInfo.skuList.forEach(function(skuInfo) {
         if (skuInfo.specIds == specIds) {
             skuId = skuInfo.id;
         }
      })
      if (!skuId && app.goodsInfo.skuNo) {
          app.goodsInfo.skuList.forEach(function(skuInfo) {
             if (skuInfo.skuNo == app.goodsInfo.skuNo) {
                 skuId = skuInfo.id;
             }
          })
      }
      if (app.goodsInfo.isSingleSpec == 'N' && skuId <= 0) {
          app.$alert("请先确认商品规格！");
          return false;
      }
      // 添加到购物车
      const cartInfo = { goodsId: app.goodsInfo.id,
                         name: app.goodsInfo.name,
                         logo: app.goodsInfo.logo,
                         price: app.goodsInfo.price,
                         skuId: skuId,
                         userId: getUserId(),
                         hangNo: app.hangNo,
                         buyNum: app.goodsNum };
      app.loading = true;
      saveCart(cartInfo).then(response => {
          app.loading = false;
          if (response.data.cartId) {
              app.getCartList();
              app.openGoodsDialog = false;
              app.goodsSpecIds = [];
              app.goodsNum = 0;
          }
      }).catch((err) => {
          app.loading = false;
          console.log(err.toString());
      });
    },
    // 删除购物车
    removeFromCart(cartId) {
      const app = this;
      removeFromCart({ cartId: [cartId], userId: getUserId() }).then(response => {
        if (response.data) {
            app.getCartList();
        }
      }).catch((err) => {
          app.loading = false;
          console.log(err.toString());
      });
    },
    // 购物车数量变化
    changeBuyNum(cartInfo) {
      const app = this;
      const param = { goodsId: cartInfo.goodsId,
                      skuId: cartInfo.skuId,
                      cartId: cartInfo.cartId,
                      action: '=',
                      userId: app.memberInfo ? app.memberInfo.id : null,
                      hangNo: app.hangNo,
                      buyNum: cartInfo.buyNum };
      app.loading = true;
      saveCart(param).then(response => {
        app.loading = false;
        if (response.data.cartId) {
            app.getCartList();
            app.openGoodsDialog = false;
            app.goodsSpecIds = [];
        }
      }).catch(() => {
          app.loading = false;
          app.getCartList();
      });
    },
    // 弹出关联会员
    switchMember() {
       this.openSwitchMemberDialog = true;
    },
    // 商品模式切换
    switchGoodMode(mode) {
      localStorage.setItem("goodsMode", mode);
      this.goodsMode = mode;
      let size = 15;
      if (mode == 'big') {
          size = 9;
      }
      this.switchTab({ limit: size });
    },
    // 弹出关联员工
    bindStaff() {
      this.openBindStaffDialog = true;
    },
    // 确认关联会员
    doSwitchMember(memberInfo) {
       this.openSwitchMemberDialog = false;
       if (memberInfo != 0) {
           this.memberInfo = memberInfo;
           if (memberInfo) {
               let cartIds = [];
               if (this.cartList && this.cartList.length > 0) {
                   this.cartList.forEach(function(cart){
                      cartIds.push(cart.cartId);
                   })
               }
               setUserId(memberInfo.id);
               this.getCartList(cartIds);
           } else {
               removeUserId();
               this.getCartList();
           }
       }
    },
    // 确认绑定员工
    doBindStaff(staff) {
      this.openBindStaffDialog = false;
      this.staffInfo = staff;
    },
    // 退出登录
    logout() {
      this.$router.push( '/' );
    },
    // 发起结算
    doSettlement() {
       if (this.cartList.length < 1) {
           this.$alert("请先添加结算商品！");
           return false;
       }
       this.getCartList();
       this.orderInfo = {};
       this.openSettlementDialog = true;
    },
    // 无商品结算
    doCashier() {
      this.orderInfo = {};
      this.openNoGoodsCashierDialog = true;
    },
    // 提交结算
    submitCashier(param) {
      this.orderInfo = {};
      this.openSettlementDialog = true;
      this.cartTotalPrice = parseFloat(param.amount);
      this.cartRemark = param.remark;
      this.openNoGoodsCashierDialog = false;
    },
    // 确认结算
    submitSettlement(param) {
      const app = this;
      // 已生成的订单支付
      if (app.orderInfo.id) {
          // 微信、支付宝支付
          if (param.payType == 'MICROPAY' || param.payType == 'ALISCAN') {
              app.payAmount = app.orderInfo.payAmount;
              app.orderId = app.orderInfo.id;
              app.openScanPayCodeDialog = true;
              app.openSettlementDialog = false;
              app.payType = param.payType;
          }
          // 现金、余额支付
          if (param.payType == 'CASH' || param.payType == 'BALANCE') {
              doPay({ orderId: app.orderId, payType: param.payType, cashierPayAmount: param.totalPrice, cashierDiscountAmount: param.discountPrice, userId: getUserId() }).then( response => {
                  app.openSettlementDialog = false;
                  if (response.data.orderInfo.payStatus == 'B') {
                      app.showPayResult({ isSuccess: true, payAmount: response.data.orderInfo.payAmount, orderId: response.data.orderInfo.id });
                  } else {
                      app.$alert(response.data.message ? response.data.message : "抱歉，订单操作异常！");
                  }
              })
          }
          return false;
      }
      // 购物车提交结算
      let cartIds = [];
      app.cartList.forEach(function(cart){
          cartIds.push(cart.cartId);
      })
      const data = { cashierPayAmount: param.totalPrice,
                     cashierDiscountAmount: param.discountPrice,
                     cartIds: cartIds.join(','),
                     orderMode: 'oneself',
                     payType: param.payType,
                     remark: param.remark,
                     type: app.cartList.length > 0 ? 'goods' : 'payment',
                     couponId: param.userCouponId ? param.userCouponId : 0,
                     couponIds: param.couponIds || '',
                     userId: getUserId(),
                     staffId: app.staffInfo ? app.staffInfo.id : 0 };
      if (app.loading) {
          return false;
      }
      app.loading = true;
      submitSettlement(data).then( response => {
          app.loading = false;
          app.doSwitchMember(null);
          app.initCashier();
          app.getCartList();
          // 微信支付，弹出扫码框
          if (response.data.orderInfo.payType == 'MICROPAY' || param.payType == 'ALISCAN') {
              app.payAmount = response.data.orderInfo.payAmount;
              app.orderId = response.data.orderInfo.id;
              app.openScanPayCodeDialog = true;
              app.openSettlementDialog = false;
              app.payType = param.payType;
              return false;
          }
          // 现金、余额支付
          if (response.data.orderInfo.payType == 'CASH' || response.data.orderInfo.payType == 'BALANCE') {
              app.openSettlementDialog = false;
              if (response.data.orderInfo.payStatus == 'B') {
                  app.showPayResult({ isSuccess: true, payAmount: response.data.orderInfo.payAmount, orderId: response.data.orderInfo.id });
              } else {
                  app.$alert(response.data.message);
              }
              return false;
          }
          if (response.code == '201') {
              app.$alert(response.data.message);
          }
          return false;
      }).catch((err) => {
          app.loading = false;
          console.log(err.toString());
      });
    },
    // 从会员管理页面触发的升级会员操作
    handleMemberUpgrade(data) {
      const app = this;
      app.$confirm(`确认帮该会员升级到「${data.gradeName}」并收费 ￥${data.payAmount}？`, '确认升级', {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        app.loading = true;
        const params = {
          type: 'member',
          targetId: data.gradeId,
          payAmount: data.payAmount,
          payType: 'MICROPAY',
          platform: app.isElectron ? 'app' : 'pc',
          userId: data.userId,
          cartIds: [],
          phone: ''
        };
        submitSettlement(params).then(response => {
          app.loading = false;
          if (response.code == 200) {
            const orderInfo = response.data.orderInfo;
            app.orderId = orderInfo.id;
            app.orderInfo = orderInfo;
            app.payAmount = orderInfo.payAmount;
            // 关联会员信息（余额支付需要）
            getMemberInfoById(data.userId).then(res => {
              if (res.data.memberInfo) {
                  app.memberInfo = res.data.memberInfo;
              }
            });
            app.openSettlementDialog = true;
          } else {
            app.$message.error(response.msg || "提交升级订单失败");
          }
        }).catch(() => {
          app.loading = false;
          app.$message.error("提交升级订单失败");
        });
      }).catch(() => {});
    },
    // 点击挂单/取单
    hangUp() {
      this.openHangUpDialog = true;
    },
    // 取单
    getHangNo(data) {
       this.openHangUpDialog = false;
       this.hangNo = data.hangNo;
       this.getCartList();
       // 关联会员信息
       if (data.hangNo && data.hangNo.length > 0 ) {
           this.doSwitchMember(data.memberInfo);
       }
    },
    // 执行挂单
    doHangUp() {
      this.hangNo = '';
      this.getCartList();
      removeUserId();
      this.memberInfo = null;
    },
    // 关闭对话框
    closeDialog(dialog) {
       if (dialog == 'settlementDialog') {
           this.openSettlementDialog = false;
       } else if (dialog == 'switchMemberDialog') {
           this.openSwitchMemberDialog = false;
       } else if (dialog == 'scanPayCodeDialog') {
           this.openScanPayCodeDialog = false;
       } else if (dialog == 'payResultDialog') {
           this.openPayResultDialog = false;
       } else if (dialog == 'printOrder') {
           this.openOrderPrintDialog = false;
       } else if (dialog == 'hangUpDialog') {
           this.openHangUpDialog = false;
       } else if (dialog == 'openNoGoodsCashierDialog') {
           this.openNoGoodsCashierDialog = false;
       } else if (dialog == 'openBindStaffDialog') {
           this.openBindStaffDialog = false;
       }
    },
    // 展示支付结果
    showPayResult(payResult) {
       this.payResult = payResult;
       this.openPayResultDialog = true;
    },
    // 订单支付
    doPayOrder(orderInfo) {
       const app = this;
       app.navTab = 0;
       app.payAmount = orderInfo.amount;
       app.orderId = orderInfo.id;
       app.orderInfo = orderInfo;
       let userId = 0
       if (orderInfo.isVisitor !== "Y") {
           userId = app.orderInfo.userInfo.id;
       }
       getMemberInfoById(userId).then(response => {
          if (response.data.memberInfo) {
              app.memberInfo = response.data.memberInfo;
          } else {
              app.memberInfo = null;
          }
       }).catch((err) => {
           app.loading = false;
           console.log(err.toString());
       });
       app.doSwitchMember(app.memberInfo);
       app.openSettlementDialog = true;
    },
    // 核销卡券
    doConfirmCoupon(code) {
      this.couponCode = code;
      this.confirmCoupon = true;
    },
    // 返回卡券列表
    doUserCoupon() {
      this.couponCode = false;
      this.confirmCoupon = false;
    },
    // 打印小票
    showOrderPrint(orderId) {
      const app = this;
      getOrderInfo(orderId).then(response => {
        if (response.data.orderInfo) {
            app.orderInfo = response.data.orderInfo;
            app.openOrderPrintDialog = true;
        }
      }).catch((err) => {
           app.loading = false;
           console.log(err.toString());
      });
    },
    // 确认关闭
    handleClose() {
      return false;
    }
  }
};
</script>

<style lang="scss" scoped>
   .main {
     height: 100%;
     width: 100%;
     display: flex;
     flex-direction: row;
     .pagination {
       position: fixed;
       bottom: 10px;
       height: 50px;
       min-width: 750px;
       line-height: 50px;
       right: 10px;
       margin-top: 10px;
       display: block;
       background: #6c757d;
       color: #ffffff;
       border-radius: 5px;
       z-index: 99999;
     }
     .top-nav {
        display: block;
        clear: both;
        height: 45px;
        width: 100%;
        background: #f5f5f5;
        border-bottom: #cccccc solid 1px;
        position: absolute;
        padding: 10px 0px 0px 40px;
        top: 0;
     }
     .left-side {
        width: 160px;
        height: 100%;
        background: #3b82f6;
        border-right: #888888 solid 2px;
        position: absolute;
        left: 0;
        top: 45px;
        padding: 0px;
        color: #FFFFFF;
        overflow-x: hidden;
        overflow-y: auto;
        display: block;
        text-align: center;
       .logo {
         height: 90px;
         padding: 20px 12px 10px 12px;
         font-weight: bold;
         .logout {
           float: left;
           height: 20px;
           line-height: 20px;
           text-align: left;
           width: 100%;
           cursor: pointer;
         }
         .store {
           font-size: 12px;
         }
         .account {
           font-size: 12px;
           border: solid 2px #FFFFFF;
           background: #6c757d;
           cursor: pointer;
           margin-top: 6px;
           padding: 2px;
           border-radius: 2px;
         }
       }
       .cate {
         text-align: center;
         margin-left: 14px;
         margin-top: 10px;
         .nav {
            list-style: none;
            display: block;
            margin: 0px;
            padding: 5px;
            text-align: center;
            .nav-item {
                margin-top: 20px;
                font-size: 14px;
                width: 120px;
                text-align: center;
               .nav-link {
                 position: relative;
                 padding: .5356875rem .9375rem;
                 white-space: nowrap;
                 text-align: center;
                 font-weight: 600;
                 color: #666666;
                 display: flex;
                 align-items: center;
                 flex-direction: column;
                 justify-content: center;
                 border-radius: 5px;
                 transition: color .2s linear;
                 background: #f5f5f5;
                 border: 2px solid #FFFFFF;
                 .cate-logo {
                   display: block;
                   width: 50px;
                   height: 50px;
                 }
               }
               .active {
                 font-weight: bold;
                 border: 4px #ff5b57 solid;
               }
            }
         }
       }
     }
     .cart-container {
        width: 310px;
        height: 100%;
        background: #FFFFFF;
        display: block;
        position: absolute;
        left: 165px;
        top: 45px;
        overflow-x: hidden;
        overflow-y: auto;
        border: #cccccc solid 1px;
        .title {
          height: 70px;
          width: 310px;
          background: #6c757d;
          border-bottom: #CCCCCC 2px solid;
          color: #FFFFFF;
          padding-top: 16px;
          padding-left: 1px;
          display: block;
          position: fixed;
          top: 45px;
          z-index: 999;
          clear: both;
          .logo {
            float: left;
            font-size: 24px;
            .avatar {
               width: 30px;
               height: 30px;
               border-radius: 40px;
               margin: 5px 0px 5px 5px;
            }
          }
          .member-info {
            float: left;
            margin-top: 5px;
            .name {
              margin-left: 2px;
              margin-right: 3px;
            }
            .none {
               margin-left: 2px;
               margin-right: 5px;
               font-size: 13px;
            }
            .switch {
              padding: 8px 8px 8px 4px;
            }
          }
        }
       .carts {
          display: block;
          width: 100%;
          color: #666666;
          margin-bottom: 100px;
          margin-top: 70px;
          padding: 0px;
          overflow: auto;
          scrollbar-width: none;
          .tab {
            width: 50%;
            .cart-list {
               margin-bottom: 60px;
              .cart-item {
                border-bottom: dashed 1px #cccccc;
                height: 110px;
                width: 310px;
                padding-top: 5px;
                .image {
                   width: 50px;
                   height: 50px;
                   margin-left: 5px;
                   border-radius: 5px;
                   border: solid 1px #ccc;
                   float: left;
                   margin-top: 20px;
                }
                .info {
                   float: left;
                   padding-left: 5px;
                   margin-top: 5px;
                   .name {
                     font-weight: bold;
                     font-size: 12px;
                     width: 150px;
                     max-height: 30px;
                     overflow: hidden;
                     display: -webkit-box;
                     -webkit-box-orient: vertical;
                     -webkit-line-clamp: 2;
                   }
                   .spec {
                     font-size: 12px;
                     width: 160px;
                     height: 20px;
                     margin-top: 2px;
                     .item {
                        margin-right: 2px;
                        border-radius: 5px;
                        text-align: center;
                        max-width: 50px;
                        height: 20px;
                        line-height: 20px;
                        float: left;
                        display: block;
                        overflow: hidden;
                        color: #606266;
                        cursor: pointer;
                        background: #cceeee;
                        padding: 0px 3px 0px 3px;
                        white-space: nowrap;
                        text-overflow: ellipsis;
                     }
                   }
                   .input {
                     width: 130px;
                     margin-top: 2px;
                   }
                }
                .option {
                   float: right;
                   text-align: right;
                   margin-right: 20px;
                   .remove {
                      margin-top: 10px;
                      font-size: 18px;
                      cursor: pointer;
                   }
                   .total {
                     margin-top: 10px;
                     font-size: 16px;
                     color: #ff5b57;
                   }
                }
              }
            }
            .empty {
              margin-top: 200px;
              width: 310px;
            }
          }
       }
       .footer {
          position: fixed;
          z-index: 999;
          background: #6c757d;
          bottom: 0;
          height: 120px;
          width: 100%;
          padding-top: 5px;
          padding-right: 15px;
          color: #FFFFFF;
          display: block;
          width: 310px;
          border: solid 3px #cccccc;
          .number {
            float: right;
            margin: 5px;
            font-size: 13px;
            .total-price {
               margin-top: 3px;
               .num {
                 color: #ff5b57;
                 font-size: 20px;
               }
            }
          }
          .options {
             text-align: center;
             cursor: pointer;
             float: right;
             color: #FFFFFF;
            .cash {
              float: left;
              height: 50px;
              border: solid 1px #3b82f6;
              padding-top: 15px;
              width: 135px;
              border-radius: 5px;
              background: #3b82f6;
              font-weight: bold;
            }
            .submit {
               float: left;
               height: 50px;
               border: solid 1px #ff5b57;
               margin-left: 10px;
               padding-top: 15px;
               width: 135px;
               border-radius: 5px;
               background: #ff5b57;
               font-weight: bold;
            }
          }
       }
     }
     .main-list {
       height: 100%;
       width: 100%;
       min-width: 760px;
       margin-left: 475px;
       margin-right: 2px;
       overflow: auto;
       display: block;
       .title {
         position: fixed;
         height: 106px;
         width: calc(100% - 465px);
         min-width: 700px;
         background: #ffffff;
         padding: 5px;
         color: red;
         top: 45px;
         .search-form {
            height: 50px;
            .form-item {
              margin-right: -2px;
            }
            .input-item {
              min-width: 456px;
            }
            .search-goods {
              height: 50px;
              background: #3b82f6;
              color: #fff;
            }
            .setting {
               float:right;
               margin-right: 20px;
               .item {
                 margin-left: 15px;
                 color: #2b2f3a;
                 cursor: pointer;
               }
               .active {
                 font-weight: bold;
                 color: #2b2f3a;
               }
               .icon {
                 width: 14px;
                 height: 14px;
                 vertical-align: middle;
                 margin-right: 2px;
               }
               .text {
                 font-size: 14px;
                 display: inline-block;
                 vertical-align: middle;
               }
            }
         }
         .tab-box {
            margin-top: 3px;
            width: 100%;
         }
       }
       .goods-list-small {
         height: 100%;
         width: 100%;
         margin-top: 148px;
         margin-left: 2px;
         overflow: auto;
         scrollbar-width: none;
         margin-bottom: 70px;
         .goods-item {
           width: 20%;
           min-height: 100px;
           min-width: 100px;
           padding: 3px;
           float: left;
           background: #ffffff;
           text-align: left;
           cursor: pointer;
           .item {
             background: #ffffff;
             padding: 5px;
             border-radius: 5px;
             border: solid 2px #888888;
             margin: 0px;
             .goods-name {
               margin-top: 6px;
               font-size: 12px;
               color: #666666;
               height: 20px;
               overflow: hidden;
               white-space: nowrap;
               text-overflow: ellipsis;
             }
             .goods-price {
               color: #ff5b57;
               font-size: 12px;
               font-weight: bold;
             }
             .image {
               width: 100%;
               height: 120px;
               border-radius: 3px;
             }
           }
         }
       }
       .goods-list-big {
         height: 100%;
         width: 100%;
         margin-top: 148px;
         margin-left: 2px;
         display: block;
         overflow: auto;
         scrollbar-width: none;
         margin-bottom: 70px;
         .goods-item {
           width: 33.3%;
           min-height: 300px;
           min-width: 220px;
           padding: 3px;
           float: left;
           text-align: left;
           cursor: pointer;
           display: block;
           .item {
             background: #ffffff;
             padding: 5px;
             border-radius: 5px;
             border: solid 2px #888888;
             margin: 0px;
             .goods-name {
               margin-top: 10px;
               font-size: 18px;
               color: #666666;
               height: 30px;
               overflow: hidden;
               white-space: nowrap;
               text-overflow: ellipsis;
             }
             .goods-price {
               color: #ff5b57;
               font-size: 18px;
               font-weight: bold;
             }
             .image {
               width: 100%;
               height: 220px;
               border-radius: 3px;
             }
           }
         }
       }
     }
   }
   .goods-info {
     border: solid 1px #ccc;
     padding: 30px;
     border-radius: 5px;
     .name {
        height: 40px;
        font-weight: bold;
        font-size: 20px;
     }
     .price {
       height: 40px;
       color: #ff5b57;
       font-size: 16px;
     }
     .spec-list {
        border: solid 1px #ccc;
        padding: 20px;
        margin-top: 10px;
        border-radius: 6px;
        .spec-item {
           margin-bottom: 20px;
          .spec-name {
             font-weight: bold;
             font-size: 16px;
          }
          .values {
             display: block;
             padding-top: 10px;
             margin-left: 0px;
             padding-left: 0px;
             font-size: 12px;
             height: auto;
             display: block;
             .value {
               border: solid 1px #cceeee;
               margin-right: 10px;
               padding: 8px 15px 5px 15px;
               cursor: pointer;
               border-radius: 4px;
               background: rgba(0, 172, 172, 0.1);
               color: #666666;
               white-space: nowrap;
               float: left;
               margin-bottom: 2px;
             }
             .active {
               border: solid 1px #ff5891;
               background: #ff5b57;
               color: #FFFFFF;
             }
          }
        }
     }
   }
</style>
<style scoped>
  .input-item >>> .el-input__inner {
    border: #3b82f6 solid 2px;
    line-height: 50px;
    height: 50px;
  }
  .form-item >>> .el-form-item__label {
    line-height: 50px;
    height: 50px;
  }
  .el-tabs--border-card {
    box-shadow: none;
    border: none;
  }
  ::v-deep .el-tabs--border-card > .el-tabs__content {
     padding: 0px;
  }
  ::v-deep .el-pagination.is-background span {
     color: #fff;
  }
</style>
