import crypto from 'crypto'
import {
  add,
  changePassword,
  changePasswordForAdmin,
  changePushKey,
  getMyPermission,
  getUserInfo,
  getUserRegion,
  login,
  logout,
  queryList,
  removeById,
  saveUserRegion
} from '@/api/user'
import {
  getToken,
  setToken,
  setName,
  removeToken,
  removeName,
  setServerId,
  removeServerId,
  getDefaultPassword,
  setDefaultPassword,
  removeDefaultPassword,
  getPermission,
  setPermission,
  removePermission
} from '@/utils/auth'
import { resetRouter } from '@/router'

const getDefaultState = () => {
  const permission = getPermission()
  return {
    token: getToken(),
    name: '',
    serverId: '',
    defaultPassword: getDefaultPassword(),
    showConfirmBoxForLoginLose: true,
    permissions: (permission && permission.permissions) || [],
    superAdmin: (permission && permission.superAdmin) || false,
    roleName: (permission && permission.roleName) || ''
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_SERVER_ID: (state, serverId) => {
    state.serverId = serverId
  },
  SET_DEFAULT_PASSWORD: (state, defaultPassword) => {
    state.defaultPassword = defaultPassword
  },
  SET_CONFIRM_BOX: (state, status) => {
    state.showConfirmBoxForLoginLose = status
  },
  SET_PERMISSIONS: (state, permissions) => {
    state.permissions = permissions || []
  },
  SET_SUPER_ADMIN: (state, superAdmin) => {
    state.superAdmin = !!superAdmin
  },
  SET_ROLE_NAME: (state, roleName) => {
    state.roleName = roleName || ''
  }
}

const actions = {
  // user login
  login({ commit, dispatch }, userInfo) {
    const { username, password } = userInfo
    return new Promise((resolve, reject) => {
      login({
        username: username.trim(),
        password: crypto.createHash('md5').update(password, 'utf8').digest('hex')
      }).then(response => {
        const { data } = response
        commit('SET_TOKEN', data.accessToken)
        commit('SET_NAME', data.username)
        commit('SET_SERVER_ID', data.serverId)
        commit('SET_DEFAULT_PASSWORD', !!data.defaultPassword)
        commit('SET_CONFIRM_BOX', true)
        setToken(data.accessToken)
        setName(data.username)
        setServerId(data.serverId)
        setDefaultPassword(data.defaultPassword)
        // 登录成功后拉取当前用户权限
        dispatch('fetchPermission').then(() => {
          resolve()
        }).catch(() => {
          resolve()
        })
      }).catch(error => {
        reject(error)
      })
    })
  },
  // user logout
  logout({ commit, state }) {
    return new Promise((resolve, reject) => {
      logout(state.token).then(() => {
        removeToken()
        removeServerId()
        removeName()
        removeDefaultPassword()
        removePermission()
        resetRouter()
        commit('RESET_STATE')
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
      removePermission()
      commit('RESET_STATE')
      resolve()
    })
  },

  // 获取当前登录用户的权限
  fetchPermission({ commit }) {
    return new Promise((resolve, reject) => {
      getMyPermission().then(response => {
        const { data } = response
        const permissions = (data && data.permissions) || []
        const superAdmin = !!(data && data.superAdmin)
        const roleName = (data && data.roleName) || ''
        commit('SET_PERMISSIONS', permissions)
        commit('SET_SUPER_ADMIN', superAdmin)
        commit('SET_ROLE_NAME', roleName)
        setPermission({ permissions: permissions, superAdmin: superAdmin, roleName: roleName })
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  getUserInfo({ commit }) {
    return new Promise((resolve, reject) => {
      getUserInfo().then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  changePushKey({ commit }, params) {
    return new Promise((resolve, reject) => {
      changePushKey(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  queryList({ commit }, params) {
    return new Promise((resolve, reject) => {
      queryList(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  removeById({ commit }, id) {
    return new Promise((resolve, reject) => {
      removeById(id).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  add({ commit }, params) {
    return new Promise((resolve, reject) => {
      add(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  changePassword({ commit }, params) {
    return new Promise((resolve, reject) => {
      changePassword(params).then(response => {
        const { data } = response
        // 修改成功后清除默认密码标记
        commit('SET_DEFAULT_PASSWORD', false)
        removeDefaultPassword()
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  changePasswordForAdmin({ commit }, params) {
    return new Promise((resolve, reject) => {
      changePasswordForAdmin(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  getUserRegion({ commit }, userId) {
    return new Promise((resolve, reject) => {
      getUserRegion(userId).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  saveUserRegion({ commit }, params) {
    return new Promise((resolve, reject) => {
      saveUserRegion(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },
  closeConfirmBoxForLoginLose({ commit }) {
    commit('SET_CONFIRM_BOX', false)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

