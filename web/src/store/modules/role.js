import { addRole, deleteRole, getAll, getAllPermission, updateRole } from '@/api/role'

const actions = {
  getAll({ commit }) {
    return new Promise((resolve, reject) => {
      getAll().then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  addRole({ commit }, params) {
    return new Promise((resolve, reject) => {
      addRole(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  updateRole({ commit }, params) {
    return new Promise((resolve, reject) => {
      updateRole(params).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  deleteRole({ commit }, id) {
    return new Promise((resolve, reject) => {
      deleteRole(id).then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  getAllPermission({ commit }) {
    return new Promise((resolve, reject) => {
      getAllPermission().then(response => {
        const { data } = response
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  }
}

export default {
  namespaced: true,
  actions
}
