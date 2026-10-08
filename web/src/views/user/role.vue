<template>
  <div id="roleManager">
    <el-dialog
      v-el-drag-dialog
      title="角色管理"
      width="60%"
      top="4vh"
      :close-on-click-modal="false"
      :visible.sync="showDialog"
      :destroy-on-close="true"
      @close="close()"
    >
      <div>
        <el-button
          v-permission="'user:edit'"
          icon="el-icon-plus"
          size="mini"
          type="primary"
          @click="addRole"
        >新增角色
        </el-button>
        <el-table
          v-loading="loading"
          :data="roleList"
          size="small"
          style="width: 100%; margin-top: 10px;"
          header-row-class-name="table-header"
        >
          <el-table-column prop="name" label="角色名称" min-width="140" />
          <el-table-column label="权限数量" min-width="120">
            <template v-slot:default="scope">
              <el-tag v-if="isSuperAdmin(scope.row)" size="medium" type="success">全部权限</el-tag>
              <span v-else>{{ permissionCount(scope.row) }} 项</span>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" min-width="160" />
          <el-table-column label="操作" min-width="160" fixed="right">
            <template v-slot:default="scope">
              <el-button
                v-permission="'user:edit'"
                :disabled="isSuperAdmin(scope.row)"
                icon="el-icon-edit"
                size="medium"
                type="text"
                @click="editRole(scope.row)"
              >编辑
              </el-button>
              <el-divider direction="vertical" />
              <el-button
                v-permission="'user:edit'"
                :disabled="isSuperAdmin(scope.row)"
                icon="el-icon-delete"
                size="medium"
                style="color: #f56c6c"
                type="text"
                @click="deleteRole(scope.row)"
              >删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-dialog
        v-el-drag-dialog
        :title="currentRole.id ? '修改角色' : '新增角色'"
        width="50%"
        top="6vh"
        :close-on-click-modal="false"
        :visible.sync="showForm"
        append-to-body
        @close="closeForm()"
      >
        <el-form ref="roleForm" :model="currentRole" :rules="rules" label-width="80px">
          <el-form-item label="角色名称" prop="name">
            <el-input v-model="currentRole.name" autocomplete="off" placeholder="请输入角色名称" />
          </el-form-item>
          <el-form-item label="权限">
            <div class="permission-container">
              <div v-for="group in permissionGroups" :key="group.group" class="permission-group">
                <div class="permission-group-title">{{ group.group }}</div>
                <el-checkbox-group v-model="currentRole.authority">
                  <el-checkbox
                    v-for="item in group.permissions"
                    :key="item.code"
                    :label="item.code"
                  >{{ item.name }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>
            </div>
          </el-form-item>
        </el-form>
        <div slot="footer">
          <el-button @click="closeForm">取消</el-button>
          <el-button type="primary" @click="onSubmit">保存</el-button>
        </div>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script>
import elDragDialog from '@/directive/el-drag-dialog'

export default {
  name: 'RoleManager',
  directives: { elDragDialog },
  data() {
    return {
      showDialog: false,
      showForm: false,
      loading: false,
      roleList: [],
      permissionGroups: [],
      currentRole: {
        id: null,
        name: null,
        authority: []
      },
      rules: {
        name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
      }
    }
  },
  methods: {
    openDialog: function() {
      this.showDialog = true
      this.initData()
    },
    initData: function() {
      this.getRoleList()
      this.getPermissionGroups()
    },
    getRoleList: function() {
      this.loading = true
      this.$store.dispatch('role/getAll')
        .then(data => {
          this.roleList = data || []
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
    getPermissionGroups: function() {
      if (this.permissionGroups.length > 0) {
        return
      }
      this.$store.dispatch('role/getAllPermission')
        .then(data => {
          this.permissionGroups = data || []
        })
        .catch(error => {
          this.$message({
            showClose: true,
            message: error,
            type: 'error'
          })
        })
    },
    isSuperAdmin: function(row) {
      return row && row.id === 1
    },
    permissionCount: function(row) {
      if (!row || !row.authority) {
        return 0
      }
      return row.authority.split(',').filter(item => !!item).length
    },
    addRole: function() {
      this.currentRole = { id: null, name: null, authority: [] }
      this.showForm = true
    },
    editRole: function(row) {
      this.currentRole = {
        id: row.id,
        name: row.name,
        authority: row.authority ? row.authority.split(',').filter(item => !!item) : []
      }
      this.showForm = true
    },
    onSubmit: function() {
      this.$refs.roleForm.validate(valid => {
        if (!valid) {
          return false
        }
        const params = {
          name: this.currentRole.name,
          authority: this.currentRole.authority.join(',')
        }
        const request = this.currentRole.id
          ? this.$store.dispatch('role/updateRole', { id: this.currentRole.id, ...params })
          : this.$store.dispatch('role/addRole', params)
        request.then(() => {
          this.$message({
            showClose: true,
            message: '保存成功',
            type: 'success'
          })
          this.showForm = false
          this.getRoleList()
        }).catch(error => {
          this.$message({
            showClose: true,
            message: error,
            type: 'error'
          })
        })
      })
    },
    deleteRole: function(row) {
      this.$confirm(`确定删除角色「${row.name}」？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        center: true,
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('role/deleteRole', row.id)
          .then(() => {
            this.$message({
              showClose: true,
              message: '删除成功',
              type: 'success'
            })
            this.getRoleList()
          })
          .catch(error => {
            this.$message({
              showClose: true,
              message: error,
              type: 'error'
            })
          })
      }).catch(() => {
      })
    },
    closeForm: function() {
      this.showForm = false
    },
    close: function() {
      this.showDialog = false
      this.roleList = []
    }
  }
}
</script>

<style scoped>
.permission-container {
  max-height: 50vh;
  overflow-y: auto;
  padding-right: 10px;
}

.permission-group {
  margin-bottom: 10px;
}

.permission-group-title {
  font-weight: bold;
  color: #606266;
  padding-bottom: 5px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 5px;
}

.permission-group .el-checkbox {
  width: 160px;
  margin-right: 10px;
}
</style>
