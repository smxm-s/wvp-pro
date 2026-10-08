<template>
  <div id="userRegion">
    <el-dialog
      v-el-drag-dialog
      title="负责区域"
      width="40%"
      top="5vh"
      :close-on-click-modal="false"
      :visible.sync="showDialog"
      :destroy-on-close="true"
      @close="close()"
    >
      <div v-loading="loading">
        <div style="margin-bottom: 10px;">
          用户：<strong>{{ username }}</strong>
        </div>
        <el-alert
          type="info"
          :closable="false"
          show-icon
          style="margin-bottom: 10px;"
        >
          <template slot="title">
            不选任何区域表示该用户不受区域限制；一旦勾选，该用户只能看到所选区域及其下级区域内的摄像头通道。
          </template>
        </el-alert>
        <div style="max-height: 50vh; overflow: auto; border: 1px solid #EBEEF5; padding: 10px; border-radius: 4px;">
          <el-tree
            ref="regionTree"
            show-checkbox
            node-key="id"
            :data="treeData"
            :props="treeProps"
            :expand-on-click-node="false"
            :check-strictly="true"
            :default-expand-all="true"
          />
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="close">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSubmit">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import elDragDialog from '@/directive/el-drag-dialog'

export default {
  name: 'UserRegion',
  directives: { elDragDialog },
  props: {},
  data() {
    return {
      showDialog: false,
      loading: false,
      saving: false,
      userId: null,
      username: '',
      treeData: [],
      listChangeCallback: null,
      // 与项目现有行政区划树保持一致的字段映射
      treeProps: {
        label: 'name',
        children: 'children'
      }
    }
  },
  computed: {},
  created() {},
  methods: {
    openDialog: function(user, callback) {
      this.listChangeCallback = callback
      this.userId = user.id
      this.username = user.username
      this.treeData = []
      this.showDialog = true
      this.loading = true
      // 并行加载：1.完整的行政区划树 2.该用户已绑定的区域
      Promise.all([
        this.loadRegionTree(null),
        this.$store.dispatch('user/getUserRegion', this.userId)
      ])
        .then(([data, regionIds]) => {
          this.treeData = data
          // 回显已勾选的区域
          this.$nextTick(() => {
            if (this.$refs.regionTree) {
              this.$refs.regionTree.setCheckedKeys(regionIds || [])
            }
          })
        })
        .catch(error => {
          this.$message({
            showClose: true,
            message: error,
            type: 'error'
          })
        })
        .finally(() => {
          this.loading = false
        })
    },
    // 递归加载行政区划树（复用项目现有的行政区划树接口）
    loadRegionTree: function(parentId) {
      return this.$store.dispatch('region/getTreeList', { parent: parentId })
        .then(list => {
          if (!list || list.length === 0) {
            return []
          }
          return Promise.all(list.map(node => {
            // 与现有区划树组件保持一致：国标编号长度大于8的节点视为叶子节点，不再向下查询
            if (node.deviceId && String(node.deviceId).length <= 8) {
              return this.loadRegionTree(node.id).then(children => {
                if (children && children.length > 0) {
                  node.children = children
                }
                return node
              })
            }
            return node
          }))
        })
    },
    onSubmit: function() {
      // 只提交"全选"节点：半选节点是"子节点被部分勾选"的父节点，
      // 若一并提交会把父级行政区划编码也纳入允许范围，导致权限被放大。
      // 后端会自动展开所选节点的所有下级区域，因此无需提交父节点。
      const checkedKeys = this.$refs.regionTree.getCheckedKeys()
      const regionIds = checkedKeys.join(',')
      this.saving = true
      this.$store.dispatch('user/saveUserRegion', {
        userId: this.userId,
        regionIds: regionIds
      })
        .then(() => {
          this.$message({
            showClose: true,
            message: '保存成功',
            type: 'success'
          })
          this.showDialog = false
          if (this.listChangeCallback) {
            this.listChangeCallback()
          }
        })
        .catch(error => {
          this.$message({
            showClose: true,
            message: error,
            type: 'error'
          })
        })
        .finally(() => {
          this.saving = false
        })
    },
    close: function() {
      this.showDialog = false
      this.treeData = []
      this.userId = null
      this.username = ''
    }
  }
}
</script>
