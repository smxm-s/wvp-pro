<template>
  <div :class="{'has-logo':showLogo}">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'

// 判断单个路由是否满足权限要求（未配置 permission 的路由默认放行）
function hasRoutePermission(route, superAdmin, permissions) {
  const permission = route.meta && route.meta.permission
  if (!permission) {
    return true
  }
  if (superAdmin) {
    return true
  }
  if (permission instanceof Array) {
    return permission.some(item => permissions.indexOf(item) > -1)
  }
  return permissions.indexOf(permission) > -1
}

// 依据权限过滤菜单树，返回用于侧边栏渲染的路由列表
function filterRoutes(routes, superAdmin, permissions) {
  const res = []
  routes.forEach(route => {
    // 当前路由自身权限不足，整条过滤
    if (!hasRoutePermission(route, superAdmin, permissions)) {
      return
    }
    const item = { ...route }
    if (item.children && item.children.length > 0) {
      // onlyIndex 类型的父级菜单只展示指定下标的子菜单
      if (item.onlyIndex >= 0) {
        const target = item.children[item.onlyIndex]
        if (!target || !hasRoutePermission(target, superAdmin, permissions)) {
          return
        }
        res.push(item)
        return
      }
      item.children = filterRoutes(item.children, superAdmin, permissions)
      // 所有可见子菜单都被过滤后，父级菜单也不再展示
      const visibleChildren = item.children.filter(child => !child.hidden)
      if (visibleChildren.length === 0) {
        return
      }
    }
    res.push(item)
  })
  return res
}

export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters([
      'sidebar',
      'superAdmin',
      'permissions'
    ]),
    routes() {
      return filterRoutes(this.$router.options.routes, this.superAdmin, this.permissions)
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },
    variables() {
      return variables
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>
