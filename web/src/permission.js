import router from './router'
import store from './store'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken, getName, getServerId, getDefaultPassword } from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login', '/play/share', '/forceChangePassword'] // no redirect whitelist

// 本次应用加载是否已刷新过权限（避免每次路由跳转都请求）
let permissionLoaded = false

router.beforeEach(async(to, from, next) => {
  // start progress bar
  NProgress.start()

  // set page title
  document.title = getPageTitle(to.meta.title)

  // determine whether the user has logged in
  const hasToken = getToken()

  if (hasToken) {
    if (to.path === '/login') {
      // if is logged in, redirect to the home page
      next({ path: '/' })
      NProgress.done()
    } else {
      const hasGetUserInfo = store.getters.name
      if (!hasGetUserInfo) {
        store.commit('user/SET_NAME', getName())
        store.commit('user/SET_SERVER_ID', getServerId())
        store.commit('user/SET_DEFAULT_PASSWORD', getDefaultPassword())
      }
      // 使用默认密码登录的用户：仅允许访问强制改密页，其他页面重定向到改密页
      if (store.state.user.defaultPassword && to.path !== '/forceChangePassword') {
        next({ path: '/forceChangePassword' })
        NProgress.done()
        return
      }
      // 应用加载后刷新一次权限（而不是每次路由跳转都请求）：
      // 这样管理员调整了角色权限后，用户刷新页面即可生效；请求失败则沿用本地缓存
      if (!permissionLoaded) {
        try {
          await store.dispatch('user/fetchPermission')
          permissionLoaded = true
        } catch (e) {
          // 拉取失败不阻塞导航，沿用本地缓存
        }
      }
      // 路由级权限校验：菜单虽然被隐藏，但直接输入地址也不能进入
      const requiredPermission = to.matched.reduce((acc, record) => {
        return (record.meta && record.meta.permission) ? record.meta.permission : acc
      }, null)
      if (requiredPermission && !store.getters.superAdmin
        && !(store.getters.permissions || []).includes(requiredPermission)) {
        next({ path: '/404' })
        NProgress.done()
        return
      }
      next()
    }
  } else {
    /* has no token*/

    if (whiteList.indexOf(to.path) !== -1) {
      // in the free login whitelist, go directly
      next()
    } else {
      // other pages that do not have permission to access are redirected to the login page.
      next(`/login?redirect=${to.path}`)
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  // finish progress bar
  NProgress.done()
})
