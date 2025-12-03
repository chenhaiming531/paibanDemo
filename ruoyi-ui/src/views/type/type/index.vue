<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="类型名称" prop="typeName">
        <el-input
          v-model="queryParams.typeName"
          placeholder="请输入类型名称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="类型描述" prop="description">
        <el-input
          v-model="queryParams.description"
          placeholder="请输入类型描述"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
          <el-option
            v-for="dict in dict.type.sys_job_status"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
          v-hasPermi="['type:type:add']"
        >新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-edit"
          size="mini"
          :disabled="single"
          @click="handleUpdate"
          v-hasPermi="['type:type:edit']"
        >修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="danger"
          plain
          icon="el-icon-delete"
          size="mini"
          :disabled="multiple"
          @click="handleDelete"
          v-hasPermi="['type:type:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['type:type:export']"
        >导出</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="typeList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="类型ID" align="center" prop="id" />
      <el-table-column label="类型名称" align="center" prop="typeName" />
      <el-table-column label="类型描述" align="center" prop="description" />
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_job_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-upload"
            @click="handleImport(scope.row)"
            v-hasPermi="['type:type:add']"
          >导入</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['type:type:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['type:type:remove']"
          >删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="queryParams.pageNum"
      :limit.sync="queryParams.pageSize"
      @pagination="getList"
    />

    <!-- 添加或修改人员类型对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="类型名称" prop="typeName">
          <el-input v-model="form.typeName" placeholder="请输入类型名称" />
        </el-form-item>
        <el-form-item label="类型描述" prop="description">
          <el-input v-model="form.description" placeholder="请输入类型描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio
              v-for="dict in dict.type.sys_job_status"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 导入对话框 -->
	<el-dialog
	  :title="'导入人员数据 - ' + currentTypeName" 
	  :visible.sync="importDialogVisible" 
	  width="500px"
	  :close-on-click-modal="false"
	  :close-on-press-escape="false"
	>
	  <el-upload
	    class="upload-demo"
	    ref="upload"
	    action=""
	    :auto-upload="false"
	    :on-change="handleFileChange"
	    :show-file-list="true"
	    accept=".xlsx,.xls"
	    :file-list="fileList"
	  >
	    <el-button slot="trigger" size="small" type="primary">选取文件</el-button>
	    <div slot="tip" class="el-upload__tip">只能上传xlsx/xls文件</div>
	  </el-upload>
	  <div slot="footer" class="dialog-footer">
	    <el-button @click="handleCancelImport">取 消</el-button>
	    <el-button type="primary" @click="submitUpload">确 定</el-button>
	  </div>
	</el-dialog>
  </div>
</template>

<script>
import { listType, getType, delType, addType, updateType, importStaff } from "@/api/type/type"
import { getToken } from "@/utils/auth"

export default {
  name: "Type",
  dicts: ['sys_job_status'],
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 人员类型表格数据
      typeList: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        typeName: null,
        description: null,
        status: null,
      },
      // 表单参数
      form: {},
      // 表单校验
      rules: {
        typeName: [
          { required: true, message: "类型名称不能为空", trigger: "blur" }
        ],
      },
      // 导入相关
      importDialogVisible: false,
      currentTypeId: null,
      currentTypeName: "",
      fileList: [],
      importUrl: process.env.VUE_APP_BASE_API + "/staff/staff/importData",
      uploadHeaders: {
        Authorization: 'Bearer ' + getToken()
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    /** 查询人员类型列表 */
    getList() {
      this.loading = true
      listType(this.queryParams).then(response => {
        this.typeList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        id: null,
        typeName: null,
        description: null,
        status: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.single = selection.length!==1
      this.multiple = !selection.length
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset()
      this.open = true
      this.title = "添加人员类型"
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      getType(id).then(response => {
        this.form = response.data
        this.open = true
        this.title = "修改人员类型"
      })
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateType(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addType(this.form).then(response => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const ids = row.id || this.ids
      this.$modal.confirm('是否确认删除人员类型编号为"' + ids + '"的数据项？').then(function() {
        return delType(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
	handleFileChange(file, fileList) {
	  this.fileList = fileList.slice(-1); // 只保留最新选择的文件
	},
    /** 导出按钮操作 */
    handleExport() {
      this.download('type/type/export', {
        ...this.queryParams
      }, `type_${new Date().getTime()}.xlsx`)
    },
    /** 导入按钮操作 */
    handleImport(row) {
      this.currentTypeId = row.id
      this.currentTypeName = row.typeName
      this.importDialogVisible = true
      this.fileList = []
    },
    /** 提交上传文件 */
	submitUpload() {
	  if (this.fileList.length === 0) {
	    this.$message.warning('请先选择文件')
	    return
	  }
	  
	  const formData = new FormData();
	  formData.append('file', this.fileList[0].raw)
	  formData.append('staffTypeId', this.currentTypeId)
	  
	  this.loading = true
	  importStaff(formData).then(response => {
	    this.$modal.msgSuccess("导入成功");
	    this.importDialogVisible = false;
	    this.getList()
	  }).catch(error => {
	    this.$modal.msgError("导入失败: " + (error.message || error))
	  }).finally(() => {
	    this.loading = false
	  });
	},
    /** 上传前校验 */
    beforeUpload(file) {
      const isExcel = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                     file.type === 'application/vnd.ms-excel'
      const isLt10M = file.size / 1024 / 1024 < 10

      if (!isExcel) {
        this.$message.error('只能上传Excel文件!')
      }
      if (!isLt10M) {
        this.$message.error('文件大小不能超过10MB!')
      }
      return isExcel && isLt10M
    },
    /** 导入成功处理 */
    handleImportSuccess(response, file, fileList) {
      if (response.code === 200) {
        this.$modal.msgSuccess(response.msg)
        this.importDialogVisible = false
        this.getList()
      } else {
        this.$modal.msgError(response.msg)
      }
    },
    /** 导入失败处理 */
    handleImportError(err, file, fileList) {
      this.$modal.msgError('导入失败: ' + (err.message || '未知错误'))
    },
	    // 修改取消导入操作的方法
	handleCancelImport() {
	    this.$refs.upload.clearFiles() // 清除已选择的文件
	    this.importDialogVisible = false // 关闭对话框
	},
  }
}
</script>

<style scoped>
.upload-demo {
  text-align: center;
}
</style>