import store from '@/store'

/**
 * 校验当前用户是否拥有指定权限码
 * @param {String|Array} value 权限码，数组时任一命中即通过
 * @returns {Boolean}
 */
function checkPermission(value) {
  if (value === undefined || value === null || value === '') {
    return true
  }
  // 超级管理员拥有所有权限
  if (store.getters.superAdmin) {
    return true
  }
  const permissions = store.getters.permissions || []
  if (value instanceof Array) {
    return value.some(item => permissions.indexOf(item) > -1)
  }
  return permissions.indexOf(value) > -1
}

export default {
  inserted(el, binding) {
    if (!checkPermission(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
