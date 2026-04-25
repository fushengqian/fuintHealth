<template>
  <view v-if="!isLoading" class="container">
    <!-- 页面头部 -->
    <view class="main-header">
      <!-- 用户信息 -->
      <view class="user-info">
        <!--头像-->
        <view class="user-avatar" @click="onUserInfo">
          <image class="image" :src="userInfo.avatar ? userInfo.avatar : '/static/default-avatar.png'"></image>
        </view>
        <view class="user-content" @click="onUserInfo">
          <!-- 会员昵称 -->
          <view class="nick-name">{{ userInfo.name ? userInfo.name : '未登录'}}</view>
          <view class="login-tips" v-if="!isLogin">(点击头像登录)</view>
          <!-- 会员等级 -->
          <view v-if="userInfo.gradeId > 0 && gradeInfo" class="user-grade">
            <view class="user-grade_icon">
              <image class="image" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAA0lBMVEUAAAD/tjL/tzH/uDP/uC7/tjH/tzH/tzL/tTH+tTL+tjP/tDD/tTD+tzD/tjL/szD/uDH/tjL/tjL+tjD/tjT/szb/tzL/tTL+uTH+tjL/tjL/tjL/tTT/tjL/tjL+tjH/uTL/vDD/tjL/tjH/tzL9uS//tTL/nBr/sS7/tjH/ujL/szD/uTv+rzf/tzL+tzH+vDP+uzL+tjP+ry7+tDL9ki/7szf/sEX/tTL/tjL+tjL/tTH/tTT/tzH/tzL/tjP/sTX/uTP/wzX+rTn/vDX9vC8m8ckhAAAAOXRSTlMAlnAMB/vjxKWGMh0S6drMiVxPRkEY9PLy0ru0sKagmo5+dGtgVCMgBP716eXWyMGxqJGRe2o5KSmFNjaYAAABP0lEQVQ4y8XS13KDMBAF0AWDDe4t7r3ETu9lVxJgJ/n/X8rKAzHG5TE+Twz3zki7I/g/KXdghIbGJewrU4yzn08Ebgl6TuZzzuOC6W5es3HX6qsSz3NFShRU0MpucytDmOSpu3yULx3CA9RD1HjVedc0jSjqm6ZzhUjDsFDQhSp/OKj5GQvg0+ZCOixsbtDLAeTTOm/yGi8GyIphIVsgH737FEDV44LJa88IRKK/SetrwT9G/GUIr6vXjoy4GXn7+RboVXnghuSjaoGecwQxL2su3CwAKlO+QFoqxI4FMctHQhQd2OhxTu184jWUlI+rMTBTn1/IQcJHQ6GQdZ7pWiDaNdhTt330efISeiqYwQEzQpTlsURJLhzkEmpCPsERfeIUVyXr6MNuIyp5uziW6xURtt7hhGwzmMNJExfO4Bd9X0ZPqAxdNwAAAABJRU5ErkJggg=="></image>
            </view>
            <view class="user-grade_name">
              <text>{{ gradeInfo.name }}</text>
            </view>
          </view>
          <!-- 会员无等级时显示手机号 -->
          <view v-else class="mobile">{{ userInfo.mobile }}</view>
          <view class="active-time" v-if="userInfo.endTime">{{ userInfo.endTime }}</view>
        </view>
        <view class="amount-info" @click="toMemberWallet(userInfo.id ? userInfo.id : 0)">
            <view class="amount-tip">余额（元）</view>
            <view class="amount-num" v-if="isLogin">{{ userInfo.balance.toFixed(2) }}</view>
            <view class="amount-num" v-if="!isLogin">0.00</view>
            <view class="point-amount" @click="onTargetPoints">积分 {{ userInfo.point ? userInfo.point : 0 }}</view>
        </view>
      </view>
      <view class="user-no">
        <view class="no" v-if="userInfo.userNo">会员号：{{ userInfo.userNo ? userInfo.userNo : '-'}}</view>
        <view class="time">{{ userInfo.createTime | timeFormat('yyyy-mm-dd hh:MM') }}</view>
      </view>
      <view class="export-btn" v-if="isLogin" @click="exportHealthReport">
        <text class="btn-text">导出健康报告</text>
      </view>
      <view class="export-btn add-record-btn" v-if="isLogin" @click="showHealthRecordPopup">
        <text class="btn-text">录入体检数据</text>
      </view>
    </view>

    <!-- 录入体检数据弹窗 -->
    <view v-if="showPopup" class="popup-mask" @click="hideHealthRecordPopup">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">录入体检数据</text>
          <text class="popup-close" @click="hideHealthRecordPopup">×</text>
        </view>
        <view class="popup-body">
          <view class="form-item">
            <view class="form-label">体检日期</view>
            <picker mode="date" :value="healthRecord.checkupDate" @change="onDateChange">
              <view class="form-input">
                <text>{{ healthRecord.checkupDate || '请选择日期' }}</text>
              </view>
            </picker>
          </view>
          <view class="form-item">
            <view class="form-label">身高 (cm)</view>
            <input class="form-input" type="digit" v-model="healthRecord.height" placeholder="请输入身高" />
          </view>
          <view class="form-item">
            <view class="form-label">体重 (kg)</view>
            <input class="form-input" type="digit" v-model="healthRecord.weight" placeholder="请输入体重" />
          </view>
          <view class="form-item">
            <view class="form-label">血压 (mmHg)</view>
            <input class="form-input" type="text" v-model="healthRecord.bloodPressure" placeholder="如：120/80" />
          </view>
          <view class="form-item">
            <view class="form-label">血糖 (mmol/L)</view>
            <input class="form-input" type="digit" v-model="healthRecord.bloodSugar" placeholder="请输入血糖值" />
          </view>
          <view class="form-item">
            <view class="form-label">备注</view>
            <textarea class="form-textarea" v-model="healthRecord.description" placeholder="请输入备注信息" />
          </view>
        </view>
        <view class="popup-footer">
          <button class="btn-cancel" @click="hideHealthRecordPopup">取消</button>
          <button class="btn-confirm" @click="saveHealthRecord">保存</button>
        </view>
      </view>
    </view>

    <!-- 积分兑换成功弹窗 -->
    <view v-if="showExchangeSuccess" class="popup-mask" @click="hideExchangeSuccessPopup">
      <view class="popup-content" @click.stop>
        <view class="popup-header">
          <text class="popup-title">兑换成功</text>
          <text class="popup-close" @click="hideExchangeSuccessPopup">×</text>
        </view>
        <view class="popup-body">
          <view class="success-icon">✓</view>
          <view class="success-text">优惠券已发放至会员账户</view>
          <view class="success-info">
            <text>优惠券：{{ exchangeResult.couponName }}</text>
          </view>
          <view class="success-info">
            <text>券码：{{ exchangeResult.code }}</text>
          </view>
          <view class="success-info">
            <text>消耗积分：{{ exchangeResult.deductPoint }}</text>
          </view>
          <view class="success-info">
            <text>剩余积分：{{ exchangeResult.remainingPoint }}</text>
          </view>
        </view>
        <view class="popup-footer">
          <button class="btn-confirm" @click="hideExchangeSuccessPopup">确定</button>
        </view>
      </view>
    </view>

    <!-- 会员资产 -->
    <view class="my-asset">
      <view class="asset-left flex-box dis-flex flex-x-center">
        <view class="asset-left-item" @click="onTargetMyCoupon('C')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ isLogin ? assets.coupon : '0' }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>优惠券</text>
          </view>
        </view>
        <view class="asset-left-item" @click="onTargetMyCoupon('P')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ isLogin ? assets.prestore : '0' }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>储值卡</text>
          </view>
        </view>
        <view class="asset-left-item" @click="onTargetMyCoupon('T')">
          <view class="item-value dis-flex flex-x-center">
            <text>{{ isLogin ? assets.timer : '0' }}</text>
          </view>
          <view class="item-name dis-flex flex-x-center">
            <text>计次卡</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 会员服务 -->
    <view class="user-service">
      <view class="service-content clearfix">
        <block v-for="(item, index) in service" :key="index">
          <view v-if="item.type == 'link'" class="service-item" @click="handleService(item)">
            <view class="item-icon">
              <text class="iconfont" :class="[`icon-${item.icon}`]"></text>
            </view>
            <view class="item-name">{{ item.name }}</view>
          </view>
          <view v-if="item.type == 'button' && $platform == 'MP-WEIXIN'" class="service-item">
            <button class="btn-normal" :open-type="item.openType">
              <view class="item-icon">
                <text class="iconfont" :class="[`icon-${item.icon}`]"></text>
              </view>
              <view class="item-name">{{ item.name }}</view>
            </button>
          </view>
        </block>
      </view>
    </view>

    <!-- 标签页导航 -->
    <view class="tab-nav">
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'info' }" 
        @click="switchTab('info')"
      >
        <text>会员信息</text>
      </view>
      <view 
        class="tab-item" 
        :class="{ active: currentTab === 'exchange' }" 
        @click="switchTab('exchange')"
      >
        <text>积分兑换</text>
      </view>
    </view>

    <!-- 积分兑换标签页内容 -->
    <view v-if="currentTab === 'exchange'" class="exchange-content">
      <view class="exchange-header">
        <view class="current-point">
          <text class="point-label">当前积分</text>
          <text class="point-value">{{ userInfo.point ? userInfo.point : 0 }}</text>
        </view>
      </view>

      <view v-if="exchangeCouponList.length === 0" class="empty-tip">
        <text>暂无可兑换的优惠券</text>
      </view>

      <view v-else class="coupon-list">
        <view 
          v-for="(coupon, index) in exchangeCouponList" 
          :key="index" 
          class="coupon-item"
        >
          <view class="coupon-left">
            <view class="coupon-amount">
              <text v-if="coupon.type === 'C'">￥</text>
              <text>{{ coupon.amount }}</text>
            </view>
            <view class="coupon-type">
              <text>{{ coupon.type === 'C' ? '优惠券' : coupon.type === 'P' ? '储值卡' : '计次卡' }}</text>
            </view>
          </view>
          <view class="coupon-middle">
            <view class="coupon-name">{{ coupon.name }}</view>
            <view class="coupon-info">
              <text class="info-item">所需积分：{{ coupon.point }}</text>
            </view>
            <view class="coupon-info">
              <text class="info-item">剩余数量：{{ coupon.leftNum }}</text>
            </view>
            <view v-if="coupon.description" class="coupon-desc">
              <text>{{ coupon.description }}</text>
            </view>
          </view>
          <view class="coupon-right">
            <button 
              class="exchange-btn" 
              :class="{ disabled: userInfo.point < coupon.point }"
              @click="handleExchange(coupon)"
            >
              <text>{{ userInfo.point >= coupon.point ? '兑换' : '积分不足' }}</text>
            </button>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
  import SettingKeyEnum from '@/common/enum/setting/Key'
  import SettingModel from '@/common/model/Setting'
  import * as MemberApi from '@/api/merchant/member'
  import * as UserApi from '@/api/user'
  import * as OrderApi from '@/api/order'
  import * as MessageApi from '@/api/message'
  import { checkLogin, showMessage } from '@/utils/app'

  /**
   * 会员服务
   * id: 标识; name: 标题名称; icon: 图标; type 类型(link和button); url: 跳转的链接
   */
  const service = [
    { id: 'order', name: '会员订单', icon: 'dingdan', type: 'link', url: 'merchantPages/order/index' },
    { id: 'rechange', name: '会员充值', icon: 'qiandai', type: 'link', url: 'merchantPages/balance/recharge' },
    { id: 'payment', name: '余额扣减', icon: 'shouhou', type: 'link', url: 'pages/pay/cashier' },
    { id: 'myCoupon', name: '卡券发放', icon: 'youhuiquan', type: 'link', url: 'merchantPages/coupon/receive' },
    { id: 'points', name: '会员积分', icon: 'jifen', type: 'link', url: 'merchantPages/points/detail' },
    { id: 'setting', name: '会员信息', icon: 'shezhi1', type: 'link', url: 'merchantPages/member/setting' },
  ]

  export default {
    data() {
      return {
        // 枚举类
        SettingKeyEnum,
        // 当前运行的终端 (此处并不冗余,因为微信小程序端view层无法直接读取$platform)
        $platform: this.$platform,
        // 正在加载
        isLoading: true,
        // 是否已登录
        isLogin: false,
        // 系统设置
        setting: {},
        // 当前用户信息
        userInfo: { id: 0, name: '', avatar: '', gradeId: 0, mobile: '', balance: 0 },
        gradeInfo: {},
        gradeEndTime: '',
        // 账户资产
        assets: { prestore: '0', timer: '0', coupon: '0' },
        // 会员服务
        service,
        // 会员ID
        memberId: 0,
        // 会员编码
        userCode: '',
        // 体检数据弹窗
        showPopup: false,
        // 体检数据
        healthRecord: {
          memberId: 0,
          height: '',
          weight: '',
          bloodPressure: '',
          bloodSugar: '',
          checkupDate: '',
          description: ''
        },
        // 当前标签页
        currentTab: 'info',
        // 可兑换优惠券列表
        exchangeCouponList: [],
        // 兑换成功弹窗
        showExchangeSuccess: false,
        // 兑换结果
        exchangeResult: {}
      }
    },

    /**
     * 生命周期函数--监听页面加载
     */
    onLoad(options) {
      // 会员ID
      this.memberId = options.memberId ? options.memberId : 0;
      // 获取页面数据
      this.getPageData();
    },

    methods: {
      // 获取页面数据
      getPageData(callback) {
        const app = this
        app.isLoading = true
        Promise.all([app.getSetting(), app.getUserInfo(), app.getUserAssets()])
          .then(result => {
            app.isLoading = false
            // 初始化我的服务数据
            app.initService()
            // 初始化订单操作数据
            app.initOrderTabbar()
            // 执行回调函数
            callback && callback()
          })
          .catch(err => {
            console.log('catch', err)
          })
      },

      // 初始化我的服务数据
      initService() {
        const app = this
        const newService = []
        service.forEach(item => {
          newService.push(item)
        })
        app.service = newService
      },

      // 获取设置
      getSetting() {
        this.setting = {}
      },

      // 获取当前用户信息
      getUserInfo() {
        const app = this;
        app.showPopup = false;
        return new Promise((resolve, reject) => {
            MemberApi.detail(app.memberId)
            .then(result => {
              if (result.data.userInfo) {
                  app.userInfo = result.data.userInfo
                  app.userCode = app.userInfo.userNo
                  app.isLogin = true
              } else {
                  app.isLogin = false
                  app.userInfo = { id: 0, name: '', avatar: '', gradeId: 0, mobile: '', balance: 0 }
              }
              app.gradeInfo = result.data.gradeInfo;
              resolve(app.userInfo);
              resolve(app.gradeInfo);
            })
            .catch(err => {
              if (err.result && err.result.status == 1001) {
                app.isLogin = false
                resolve(null)
              } else {
                reject(err)
              }
            })
        })
      },

      // 获取账户资产
      getUserAssets() {
        const app = this
        return new Promise((resolve, reject) => {
            UserApi.assets({ userId: app.memberId })
            .then(result => {
              app.assets = result.data.asset
              resolve(app.assets)
            })
            .catch(err => {
              if (err.result && err.result.status == 1001) {
                app.isLogin = false
                resolve(null)
              } else {
                reject(err)
              }
            })
        })
      },
      
      // 跳转我的余额
      toMemberWallet(userId) {
          return false;
      },
      
      // 跳转充值
      toRecharge(userId) {
          return false;
      },

      // 跳转到我的积分页面
      onTargetPoints() {
         return false;
      },

      // 跳转到我的卡券列表页
      onTargetMyCoupon(type) {
         this.$navTo('pages/my-coupon/index?type='+type+'&memberId='+this.memberId)
      },
      
      // 跳转会员设置页面
      onUserInfo() {
          return false;
      },

      // 跳转到服务页面
      handleService({ url }) {
          const app = this;
          console.log(app.userCode)
          app.$navTo(url + '?memberId=' + app.memberId + '&code=' + app.userCode);
      },

      // 导出健康报告
      exportHealthReport() {
        const app = this;
        uni.showLoading({ title: '正在生成报告...' });
        MemberApi.exportHealthReport(app.memberId)
          .then(tempFilePath => {
            uni.hideLoading();
            uni.openDocument({
              filePath: tempFilePath,
              fileType: 'pdf',
              showMenu: true,
              success: (res) => {
                console.log('打开文档成功', res);
              },
              fail: (err) => {
                console.error('打开文档失败', err);
                uni.showToast({ title: '打开文档失败', icon: 'none' });
              }
            });
          })
          .catch(err => {
            uni.hideLoading();
            console.error('导出失败', err);
            uni.showToast({ title: '导出失败，请稍后重试', icon: 'none' });
          });
      },

      // 显示录入体检数据弹窗
      showHealthRecordPopup() {
        const app = this;
        const today = new Date();
        const year = today.getFullYear();
        const month = String(today.getMonth() + 1).padStart(2, '0');
        const day = String(today.getDate()).padStart(2, '0');
        app.healthRecord = {
          memberId: app.memberId,
          height: '',
          weight: '',
          bloodPressure: '',
          bloodSugar: '',
          checkupDate: `${year}-${month}-${day}`,
          description: ''
        };
        app.showPopup = true;
      },

      // 隐藏录入体检数据弹窗
      hideHealthRecordPopup() {
        this.showPopup = false;
      },

      // 日期选择改变
      onDateChange(e) {
        this.healthRecord.checkupDate = e.detail.value;
      },

      // 保存体检数据
      saveHealthRecord() {
        const app = this;
        
        if (!app.healthRecord.height && !app.healthRecord.weight && 
            !app.healthRecord.bloodPressure && !app.healthRecord.bloodSugar) {
          uni.showToast({ title: '请至少填写一项体检数据', icon: 'none' });
          return;
        }

        // 身高校验：50-250cm
        if (app.healthRecord.height) {
          const height = parseFloat(app.healthRecord.height);
          if (isNaN(height) || height < 50 || height > 250) {
            uni.showToast({ title: '身高范围应为 50-250cm', icon: 'none' });
            return;
          }
        }

        // 体重校验：20-300kg
        if (app.healthRecord.weight) {
          const weight = parseFloat(app.healthRecord.weight);
          if (isNaN(weight) || weight < 20 || weight > 300) {
            uni.showToast({ title: '体重范围应为 20-300kg', icon: 'none' });
            return;
          }
        }

        // 血压校验：格式必须为"数字/数字"
        if (app.healthRecord.bloodPressure) {
          const bpPattern = /^\d+\/\d+$/;
          if (!bpPattern.test(app.healthRecord.bloodPressure)) {
            uni.showToast({ title: '血压格式应为"数字/数字"，如：120/80', icon: 'none' });
            return;
          }
        }

        // 血糖校验：1-30
        if (app.healthRecord.bloodSugar) {
          const bloodSugar = parseFloat(app.healthRecord.bloodSugar);
          if (isNaN(bloodSugar) || bloodSugar < 1 || bloodSugar > 30) {
            uni.showToast({ title: '血糖范围应为 1-30', icon: 'none' });
            return;
          }
        }

        uni.showLoading({ title: '保存中...' });
        MemberApi.saveHealthRecord({
          memberId: app.memberId,
          height: app.healthRecord.height,
          weight: app.healthRecord.weight,
          bloodPressure: app.healthRecord.bloodPressure,
          bloodSugar: app.healthRecord.bloodSugar,
          checkupDate: app.healthRecord.checkupDate,
          description: app.healthRecord.description
        })
          .then(result => {
            uni.hideLoading();
            uni.showToast({ title: '保存成功', icon: 'success' });
            app.hideHealthRecordPopup();
            setTimeout(() => {
              app.getPageData();
            }, 1500);
          })
          .catch(err => {
            uni.hideLoading();
            console.error('保存失败', err);
            uni.showToast({ title: '保存失败，请稍后重试', icon: 'none' });
          });
      },

      // 切换标签页
      switchTab(tab) {
        const app = this;
        app.currentTab = tab;
        if (tab === 'exchange') {
          app.getExchangeableCoupons();
        }
      },

      // 获取可兑换优惠券列表
      getExchangeableCoupons() {
        const app = this;
        if (!app.isLogin || !app.memberId) {
          return;
        }
        MemberApi.getExchangeableCoupons(app.memberId)
          .then(result => {
            if (result.data && result.data.couponList) {
              app.exchangeCouponList = result.data.couponList;
              if (result.data.userPoints !== undefined && result.data.userPoints !== null) {
                app.userInfo.point = result.data.userPoints;
              }
            } else {
              app.exchangeCouponList = [];
            }
          })
          .catch(err => {
            console.error('获取可兑换优惠券列表失败', err);
            uni.showToast({ title: '获取数据失败，请稍后重试', icon: 'none' });
          });
      },

      // 处理兑换操作
      handleExchange(coupon) {
        const app = this;
        if (!app.isLogin) {
          uni.showToast({ title: '请先登录', icon: 'none' });
          return;
        }

        const userPoints = app.userInfo.point ? app.userInfo.point : 0;
        if (userPoints < coupon.point) {
          uni.showToast({ title: '积分不足', icon: 'none' });
          return;
        }

        uni.showModal({
          title: '确认兑换',
          content: `确认使用 ${coupon.point} 积分兑换「${coupon.name}」吗？`,
          success: (res) => {
            if (res.confirm) {
              app.doExchange(coupon);
            }
          }
        });
      },

      // 执行兑换
      doExchange(coupon) {
        const app = this;
        uni.showLoading({ title: '兑换中...' });

        MemberApi.exchangeCoupon({
          memberId: app.memberId,
          couponId: coupon.id
        })
          .then(result => {
            uni.hideLoading();
            if (result.data && result.data.success) {
              app.exchangeResult = result.data;
              app.showExchangeSuccess = true;
              app.userInfo.point = result.data.remainingPoint;
              app.getExchangeableCoupons();
            } else {
              uni.showToast({ title: result.message || '兑换失败', icon: 'none' });
            }
          })
          .catch(err => {
            uni.hideLoading();
            console.error('兑换失败', err);
            const errorMsg = err.result && err.result.message ? err.result.message : '兑换失败，请稍后重试';
            uni.showToast({ title: errorMsg, icon: 'none' });
          });
      },

      // 隐藏兑换成功弹窗
      hideExchangeSuccessPopup() {
        this.showExchangeSuccess = false;
      }
    },

    /**
     * 下拉刷新
     */
    onPullDownRefresh() {
      // 获取首页数据
      this.getPageData(() => {
        uni.stopPullDownRefresh()
      })
    }
  }
</script>

<style lang="scss" scoped>
  // 页面头部
  .main-header {
    background: $fuint-theme;
    height: 380rpx;
    background-size: cover;
    overflow: hidden;
    display: block;
    align-items: center;
    margin: 10rpx 25rpx 10rpx 25rpx;
    border-radius: 10rpx;
    
    .user-info {
      display: block;
      height: 200rpx;
      margin: 20rpx;
      margin-left: 20rpx;
      .user-avatar {
          padding-top: 10rpx;
          width: 50rpx;
          margin-top: 70rpx;
          float: left;
          .image {
              display: block;
              width: 100rpx;
              height: 100rpx;
              border-radius: 999rpx;
          }
      }
      
      .user-content {
        display: block;
        justify-content: center;
        margin-top: 80rpx;
        margin-left: 60rpx;
        float: left;
        color: #ffffff;
        max-width: 300rpx;
        .nick-name {
            font-size: 32rpx;
            font-weight: bold;
            max-width: 270rpx;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .mobile {
          margin-top: 15rpx;
          font-size: 26rpx;
        }

        .user-grade {
          display: block;
          align-items: center;
          background: #3c3c3c;
          margin-top: 8rpx;
          border-radius: 10rpx;
          padding: 5rpx 12rpx;
          width: 80%;
          min-width: 160rpx;
          height: 40rpx;

          .user-grade_icon .image {
            display: block;
            width: 32rpx;
            height: 32rpx;
            float: left;
          }

          .user-grade_name {
            margin-left: 5rpx;
            font-size: 24rpx;
            color: #EEE0C3;
            float: left;
          }
        }
        .active-time {
            margin-top: 3rpx;
        }

        .login-tips {
          margin-top: 9rpx;
          font-size: 25rpx;
        }
      }
      .amount-info {
          margin-top: 80rpx;
          margin-left: 70rpx;
          color: #fff;
          display: block;
          float: left;
          max-width: 120rpx;
          .amount-tip {
              font-size: 24rpx;
          }
          .amount-num {
              margin-top: 10rpx;
              font-weight: bold;
              font-size: 48rpx;
          }
          .point-amount {
              display: block;
              margin-top: 2px;
              width: 100px;
          }
      }
    }
    .user-no {
        display: block;
        font-size: 28rpx;
        margin: 110rpx 0rpx 0rpx 20rpx;
        color: #ffffff;
        .no {
            float: left;
            margin-bottom: 8rpx;
        }
        .time {
            margin-right: 20rpx;
            color: #f5f5f5;
            float: right;
        }
    }
    .export-btn {
        display: flex;
        justify-content: center;
        align-items: center;
        margin: 20rpx 20rpx 0rpx 20rpx;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 8rpx;
        padding: 16rpx 0;
        .btn-text {
            color: #ffffff;
            font-size: 28rpx;
            font-weight: bold;
        }
    }
  }

  // 我的资产
  .my-asset {
    display: flex;
    background: #fff;
    margin: 10rpx 20rpx 10rpx 20rpx;
    padding: 40rpx 0;
    border: 2rpx #f5f5f5 solid;

    .asset-right {
      width: 200rpx;
      border-left: 1rpx solid #eee;
    }
    .asset-left-item {
      text-align: center;
      color: #666;
      padding: 0 72rpx;
      width: 33%;

      .item-value {
        font-size: 35rpx;
        color: #f03c3c;
        font-weight: bold;
      }
      
      .item-name {
        font-size: 25rpx;
        margin-top: 6rpx;
      }
    }

  }

  // 会员服务
  .user-service {
    margin: 0rpx auto 20rpx auto;
    border: 2rpx #f5f5f5 solid;
    background: #FFF;
    padding: 30rpx 0rpx;
    width: 94%;
    box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
    border-radius: 5rpx;
    display: block;

    .service-content {
       margin-top: 20rpx;
      .service-item {
        width: 25%;
        float: left;
        margin-bottom: 25rpx;

        .item-icon {
          text-align: center;
          margin: 0 auto;
          padding: 10rpx 0;
          color: #ff3800;
          font-size: 40rpx;
        }

        .item-name {
          font-size: 24rpx;
          color: #545454;
          text-align: center;
          margin-right: 10rpx;
        }

      }
    }
  }
  
  // 推荐信息
  .my-recommend {
      height: 20rpx;
  }
  
  // 会员升级
  .member-update {
      margin: 22rpx auto 0rpx auto;
      padding: 20rpx 0;
      border-radius: 5rpx;
      box-shadow: 0 1rpx 5rpx 0px rgba(0, 0, 0, 0.05);
      background: #fff;
      width: 94%;
      text-align: center;
      .update-title {
        padding-left: 20rpx;
        margin-bottom: 30rpx;
        font-size: 28rpx;
        text-align: left;
      }
      .recharge {
            position: relative;
            margin-bottom: 35rpx;
            display: flex;
            flex-direction: row;
            align-items: center;
            
            &-tag {
                position: absolute;
                top: -2rpx;
                left: -2rpx;
                width: 170rpx;
                height: 36rpx;
                display: flex;
                flex-direction: row;
                align-items: center;
                justify-content: center;
                background-image: url('~@/static/user/tag.png');
                background-size: 100%;
                &-text {
                    font-size: 20rpx;
                    color: #FFFFFF;
                    text-align: center;
                }
            }
            
            &-item {
                position: relative;
                padding: 40rpx 0;
                margin-left: 15rpx;
                width: 29.33%;
                height: 270rpx;
                flex-shrink: 0;
                display: flex;
                flex-direction: column;
                align-items: center;
                border: solid 1rpx #CBCCCE;
                border-radius: 12rpx;
                
                &-active {
                    border: solid 2rpx #EDD2A9;
                    background-color: #FBF1E5;
                }
                
                &-duration {
                    margin-bottom: 30rpx;
                    font-size: 26rpx;
                    color: #1C1C1C;
                }
                
                &-price {
                    margin-bottom: 20rpx;
                    display: flex;
                    flex-direction: row;
                    align-items: baseline;
                    
                    &-text {
                        font-size: 48rpx;
                        color: #E3BE83;
                    }
                }
                
                &-des {
                    font-size: 22rpx;
                    color: #A5A3A2;
                }
            }
        }
    }
  }

  // 录入体检数据弹窗
  .popup-mask {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 9999;

    .popup-content {
      width: 650rpx;
      background: #fff;
      border-radius: 16rpx;
      max-height: 80vh;
      overflow-y: auto;

      .popup-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 30rpx;
        border-bottom: 1rpx solid #eee;

        .popup-title {
          font-size: 32rpx;
          font-weight: bold;
          color: #333;
        }

        .popup-close {
          font-size: 48rpx;
          color: #999;
          line-height: 1;
        }
      }

      .popup-body {
        padding: 30rpx;

        .form-item {
          margin-bottom: 30rpx;

          .form-label {
            font-size: 28rpx;
            color: #333;
            margin-bottom: 12rpx;
            display: block;
          }

          .form-input {
            height: 80rpx;
            line-height: 80rpx;
            padding: 0 20rpx;
            background: #f5f5f5;
            border-radius: 8rpx;
            font-size: 28rpx;
            color: #333;
          }

          .form-textarea {
            height: 120rpx;
            padding: 20rpx;
            background: #f5f5f5;
            border-radius: 8rpx;
            font-size: 28rpx;
            color: #333;
          }
        }
      }

      .popup-footer {
        display: flex;
        padding: 30rpx;
        border-top: 1rpx solid #eee;

        .btn-cancel,
        .btn-confirm {
          flex: 1;
          height: 88rpx;
          line-height: 88rpx;
          text-align: center;
          border-radius: 44rpx;
          font-size: 30rpx;
          margin: 0 10rpx;
        }

        .btn-cancel {
          background: #f5f5f5;
          color: #666;
        }

        .btn-confirm {
          background: $fuint-theme;
          color: #fff;
        }
      }
    }
  }

  // 标签页导航
  .tab-nav {
    display: flex;
    background: #fff;
    margin: 10rpx 20rpx;
    border-radius: 10rpx;
    overflow: hidden;
    border: 2rpx #f5f5f5 solid;

    .tab-item {
      flex: 1;
      text-align: center;
      padding: 30rpx 0;
      font-size: 28rpx;
      color: #666;
      position: relative;

      &.active {
        color: $fuint-theme;
        font-weight: bold;

        &::after {
          content: '';
          position: absolute;
          bottom: 0;
          left: 50%;
          transform: translateX(-50%);
          width: 60rpx;
          height: 4rpx;
          background: $fuint-theme;
          border-radius: 2rpx;
        }
      }
    }
  }

  // 积分兑换内容
  .exchange-content {
    background: #fff;
    margin: 10rpx 20rpx 20rpx;
    border-radius: 10rpx;
    border: 2rpx #f5f5f5 solid;
    min-height: 400rpx;

    .exchange-header {
      padding: 30rpx;
      border-bottom: 1rpx solid #f5f5f5;
      display: flex;
      justify-content: space-between;
      align-items: center;

      .current-point {
        display: flex;
        align-items: baseline;

        .point-label {
          font-size: 28rpx;
          color: #666;
          margin-right: 10rpx;
        }

        .point-value {
          font-size: 40rpx;
          font-weight: bold;
          color: $fuint-theme;
        }
      }
    }

    .empty-tip {
      padding: 80rpx 0;
      text-align: center;
      font-size: 28rpx;
      color: #999;
    }
  }

  // 优惠券列表
  .coupon-list {
    padding: 20rpx;

    .coupon-item {
      display: flex;
      margin-bottom: 20rpx;
      background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
      border-radius: 12rpx;
      overflow: hidden;
      position: relative;

      .coupon-left {
        width: 180rpx;
        padding: 30rpx 10rpx;
        text-align: center;
        position: relative;

        &::after {
          content: '';
          position: absolute;
          right: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 1rpx;
          height: 60%;
          border-right: 2rpx dashed rgba(255, 255, 255, 0.3);
        }

        .coupon-amount {
          color: #fff;
          font-weight: bold;
          line-height: 1;

          text:first-child {
            font-size: 28rpx;
          }

          text:last-child {
            font-size: 48rpx;
          }
        }

        .coupon-type {
          margin-top: 10rpx;
          font-size: 22rpx;
          color: rgba(255, 255, 255, 0.8);
        }
      }

      .coupon-middle {
        flex: 1;
        padding: 25rpx 20rpx;
        display: flex;
        flex-direction: column;
        justify-content: center;

        .coupon-name {
          font-size: 30rpx;
          font-weight: bold;
          color: #fff;
          margin-bottom: 10rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .coupon-info {
          margin-bottom: 6rpx;

          .info-item {
            font-size: 24rpx;
            color: rgba(255, 255, 255, 0.9);
          }
        }

        .coupon-desc {
          font-size: 22rpx;
          color: rgba(255, 255, 255, 0.7);
          margin-top: 6rpx;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .coupon-right {
        width: 140rpx;
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 0 15rpx;

        .exchange-btn {
          width: 100%;
          padding: 16rpx 0;
          background: #fff;
          border-radius: 30rpx;
          font-size: 26rpx;
          color: #ee5a5a;
          font-weight: bold;
          border: none;
          line-height: 1;

          &.disabled {
            background: rgba(255, 255, 255, 0.6);
            color: #999;
          }
        }
      }

      &::before,
      &::after {
        content: '';
        position: absolute;
        width: 24rpx;
        height: 24rpx;
        border-radius: 50%;
        background: #f5f5f5;
        left: 168rpx;
        z-index: 1;
      }

      &::before {
        top: -12rpx;
      }

      &::after {
        bottom: -12rpx;
      }
    }
  }

  // 兑换成功弹窗样式
  .popup-mask {
    .popup-content {
      .success-icon {
        width: 100rpx;
        height: 100rpx;
        line-height: 100rpx;
        text-align: center;
        margin: 20rpx auto;
        background: #52c41a;
        border-radius: 50%;
        color: #fff;
        font-size: 48rpx;
        font-weight: bold;
      }

      .success-text {
        text-align: center;
        font-size: 32rpx;
        font-weight: bold;
        color: #333;
        margin-bottom: 30rpx;
      }

      .success-info {
        padding: 16rpx 0;
        font-size: 28rpx;
        color: #666;
        text-align: center;

        text {
          display: inline-block;
        }
      }
    }
  }
</style>
