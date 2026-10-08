import request from '@/utils/request'

// 云端录像API

export function getAll() {
  return request({
    method: 'get',
    url: '/api/role/all'
  })
}

export function addRole(params) {
  const { name, authority } = params
  return request({
    method: 'post',
    url: '/api/role/add',
    params: {
      name: name,
      authority: authority
    }
  })
}

export function updateRole(params) {
  const { id, name, authority } = params
  return request({
    method: 'post',
    url: '/api/role/update',
    params: {
      id: id,
      name: name,
      authority: authority
    }
  })
}

export function deleteRole(id) {
  return request({
    method: 'delete',
    url: `/api/role/delete?id=${id}`
  })
}

export function getAllPermission() {
  return request({
    method: 'get',
    url: '/api/role/permission/all'
  })
}

