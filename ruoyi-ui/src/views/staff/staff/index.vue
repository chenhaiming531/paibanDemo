<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="人员姓名" prop="staffName">
        <el-input
          v-model="queryParams.staffName"
          placeholder="请输入人员姓名"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="人员类型" prop="staffTypeId">
        <el-select
          v-model="queryParams.staffTypeId"
          placeholder="请选择人员类型"
          clearable
          filterable
        >
          <el-option
            v-for="item in staffTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="性别" prop="staffGender">
        <el-select v-model="queryParams.staffGender" placeholder="请选择性别" clearable>
          <el-option
            v-for="dict in dict.type.sys_user_sex"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="科室" prop="department">
        <el-input
          v-model="queryParams.department"
          placeholder="请输入科室"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="单位" prop="unit">
        <el-input
          v-model="queryParams.unit"
          placeholder="请输入单位"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="职称" prop="jobTitle">
        <el-input
          v-model="queryParams.jobTitle"
          placeholder="请输入职称"
          clearable
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <el-form-item label="在院时间" prop="hospitalTime">
        <el-input
          v-model="queryParams.hospitalTime"
          placeholder="请输入在院时间(年)"
          clearable
          type="number"
          @keyup.enter.native="handleQuery"
        />
      </el-form-item>
      <!-- 分离入职时间和离职时间搜索 -->
      <el-form-item label="生效时间" prop="beginHospitalTime">
        <el-date-picker
          v-model="queryParams.beginHospitalTime"
          type="date"
          placeholder="选择生效时间"
          value-format="yyyy-MM-dd"
          clearable
          style="width: 140px"
        />
      </el-form-item>
      <el-form-item label="失效时间" prop="endHospitalTime">
        <el-date-picker
          v-model="queryParams.endHospitalTime"
          type="date"
          placeholder="选择失效时间"
          value-format="yyyy-MM-dd"
          clearable
          style="width: 140px"
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
<!--      <el-form-item label="标签" prop="staffLeave">
        <el-select v-model="queryParams.staffLeave" placeholder="请选择标签" clearable>
          <el-option
            v-for="dict in dict.type.leave"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item> -->
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
          v-hasPermi="['staff:staff:add']"
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
          v-hasPermi="['staff:staff:edit']"
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
          v-hasPermi="['staff:staff:remove']"
        >删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
          v-hasPermi="['staff:staff:export']"
        >导出</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="info"
          plain
          icon="el-icon-price-tag"
          size="mini"
          :disabled="multiple"
          @click="handleTag"
          v-hasPermi="['staff:staff:edit']"
        >调整休息</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="success"
          plain
          icon="el-icon-view"
          size="mini"
          :disabled="multiple"
          @click="handleViewLeave"
          v-hasPermi="['staff:staff:query']"
        >查看休假</el-button>
      </el-col>
<!--      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-close"
          size="mini"
          :disabled="multiple"
          @click="handleCancelTag"
          v-hasPermi="['staff:staff:edit']"
        >取消标签</el-button>
      </el-col> -->
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="staffList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="人员ID" align="center" prop="id" /> -->
      <el-table-column label="人员姓名" align="center" prop="staffName" />
      <el-table-column label="人员类型" align="center" prop="staffTypeId">
        <template slot-scope="scope">
          {{ getStaffTypeName(scope.row.staffTypeId) }}
        </template>
      </el-table-column>
      <el-table-column label="性别" align="center" prop="staffGender">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_user_sex" :value="scope.row.staffGender"/>
        </template>
      </el-table-column>
      <el-table-column label="科室" align="center" prop="department" />
      <el-table-column label="单位" align="center" prop="unit" />
      <el-table-column label="职称" align="center" prop="jobTitle" />
      <el-table-column label="在院年限" align="center" prop="hospitalTime" />
      <el-table-column label="排班排序" align="center" prop="staffSort" />
      <el-table-column label="值班排序" align="center" prop="staffDutySort" />
      <el-table-column label="优先级" align="center" prop="staffPriority" />
      <el-table-column label="生效时间" align="center" prop="beginHospitalTime" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.beginHospitalTime, '{y}-{m}-{d}') }}</span>
        </template>
      </el-table-column>
      <el-table-column label="失效时间" align="center" prop="endHospitalTime" width="150">
        <template slot-scope="scope">
          <span>{{ scope.row.endHospitalTime ? parseTime(scope.row.endHospitalTime, '{y}-{m}-{d}') : '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.sys_job_status" :value="scope.row.status"/>
        </template>
      </el-table-column>
<!--      <el-table-column label="标签" align="center" prop="staffLeave">
        <template slot-scope="scope">
          <dict-tag :options="dict.type.leave" :value="scope.row.staffLeave"/>
        </template>
      </el-table-column> -->
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleUpdate(scope.row)"
            v-hasPermi="['staff:staff:edit']"
          >修改</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-delete"
            @click="handleDelete(scope.row)"
            v-hasPermi="['staff:staff:remove']"
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

    <!-- 添加或修改排班人员信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="人员姓名" prop="staffName">
          <el-input v-model="form.staffName" placeholder="请输入人员姓名" />
        </el-form-item>
        <el-form-item label="人员类型" prop="staffTypeId">
          <el-select
            v-model="form.staffTypeId"
            placeholder="请选择人员类型"
            clearable
            filterable
          >
            <el-option
              v-for="item in staffTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="性别" prop="staffGender">
          <el-radio-group v-model="form.staffGender">
            <el-radio
              v-for="dict in dict.type.sys_user_sex"
              :key="dict.value"
              :label="dict.value"
            >{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="科室" prop="department">
          <el-input v-model="form.department" placeholder="请输入科室" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="职称" prop="jobTitle">
          <el-input v-model="form.jobTitle" placeholder="请输入职称" />
        </el-form-item>
<!--        <el-form-item label="标签" prop="staffLeave">
          <el-select v-model="form.staffLeave" placeholder="请选择标签" clearable>
            <el-option
              v-for="dict in dict.type.leave"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item> -->
        <el-form-item label="在院年限" prop="hospitalTime">
          <el-input v-model="form.hospitalTime" placeholder="请输入在院年限" type="number" :min="0" />
        </el-form-item>
        <el-form-item label="排班排序" prop="staffSort">
          <el-input v-model="form.staffSort" placeholder="排班排序" type="number" :min="0" />
        </el-form-item>
        <el-form-item label="值班排序" prop="staffDutySort">
          <el-input v-model="form.staffDutySort" placeholder="值班排序" type="number" :min="0" />
        </el-form-item>
        <el-form-item label="优先级" prop="staffPriority">
          <el-input v-model="form.staffPriority" placeholder="优先级" type="number" :min="0" />
        </el-form-item>
        <el-form-item label="生效时间" prop="beginHospitalTime">
          <el-date-picker
            v-model="form.beginHospitalTime"
            type="date"
            placeholder="选择生效时间"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="失效时间" prop="endHospitalTime">
          <el-date-picker
            v-model="form.endHospitalTime"
            type="date"
            placeholder="选择失效时间"
            value-format="yyyy-MM-dd"
            style="width: 100%"
          />
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
    <!-- 设置标签对话框 -->
 <!-- 设置标签对话框 -->
    <el-dialog title="调整休息" :visible.sync="tagOpen" width="600px" append-to-body>
      <el-form :model="tagForm" label-width="100px">
        <el-form-item label="休假类型">
          <el-select
            v-model="tagForm.staffLeave"
            placeholder="请选择休假类型"
            clearable
            @change="handleTagChange"
          >
            <el-option
              v-for="dict in dict.type.leave"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>

        <!-- 休假时间段选择区域 -->
        <el-form-item
          v-if="showTimeRange"
          label="休假时间"
          prop="timeRanges"
        >
          <div v-for="(range, index) in tagForm.timeRanges" :key="index" style="margin-bottom: 10px;">
            <el-row :gutter="10" align="middle">
              <el-col :span="20">
                <el-date-picker
                  v-model="range.timeRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="休假开始日期"
                  end-placeholder="休假结束日期"
                  value-format="yyyy-MM-dd"
                  style="width: 100%;"
                />
              </el-col>
              <el-col :span="4">
                <el-button
                  v-if="tagForm.timeRanges.length > 1"
                  type="danger"
                  icon="el-icon-delete"
                  circle
                  size="mini"
                  @click="removeTimeRange(index)"
                ></el-button>
              </el-col>
            </el-row>
          </div>

          <el-button
            type="primary"
            icon="el-icon-plus"
            size="mini"
            @click="addTimeRange"
            style="margin-top: 10px;"
          >添加休假时间段</el-button>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitTagForm">确 定</el-button>
        <el-button @click="tagOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 查看休假对话框 -->
    <el-dialog title="休假信息" :visible.sync="leaveViewOpen" width="600px" append-to-body>
      <el-table :data="currentStaffLeaveInfo" v-loading="leaveViewLoading" border="">
        <el-table-column prop="staffName" label="人员姓名" align="center" />
        <el-table-column prop="staffLeave" label="休假类型" align="center">
          <template slot-scope="scope">
            <dict-tag :options="dict.type.leave" :value="scope.row.staffLeave"/>
          </template>
        </el-table-column>
        <el-table-column prop="staffLeaveStartTime" label="开始日期" align="center" />
        <el-table-column prop="staffLeaveEndTime" label="结束日期" align="center" />
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="leaveViewOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listStaff, getStaff, delStaff, addStaff, updateStaff ,batchTagStaff ,batchCancelTag} from "@/api/staff/staff"
import { listType } from "@/api/type/type"

export default {
  name: "Staff",
  dicts: ['sys_job_status', 'sys_user_sex','leave'],
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
      // 排班人员信息表格数据
      staffList: [],
      // 人员类型下拉选项
      staffTypeOptions: [],
      // 弹出层标题
      title: "",
      // 是否显示弹出层
      open: false,
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        staffName: null,
        staffTypeId: null,
        staffGender: null,
        department: null,
        unit: null,
        jobTitle: null,
        hospitalTime: null,
        staffSort:null,
        staffDutySort:null,
        beginHospitalTime: null,  // 入职时间查询参数
        endHospitalTime: null,    // 离职时间查询参数
        status: null,
        staffLeave: null,         // 请假状态查询参数
      },
      // 表单参数
      form: {
        id: null,
        staffName: null,
        staffTypeId: null,
        staffGender: null,
        department: null,
        unit: null,
        jobTitle: null,
        staffLeave: null,         // 请假状态字段
        hospitalTime: null,
        staffSort:null,
        staffDutySort:null,
        beginHospitalTime: null,
        endHospitalTime: null,
        status: null,
        createBy: null,
        createTime: null,
        updateBy: null,
        updateTime: null
      },
      // 表单校验
      rules: {
        staffName: [
          { required: true, message: "人员姓名不能为空", trigger: "blur" }
        ],
        // staffTypeId: [
        //   { required: true, message: "人员类型不能为空", trigger: "change" }
        // ]
        // 可以根据需要添加staffLeave的验证规则
        // staffLeave: [
        //   { required: true, message: "请假状态不能为空", trigger: "change" }
        // ]
      },
          tagOpen: false, // 控制标签对话框显示
          tagForm: {
            staffLeave: null ,// 选择的标签值
            timeRanges: [{ timeRange: [] }] // 时间段数组
          },
          // 需要显示时间段的标签值（根据实际情况调整）
                timeRangeTags: ['1', '2'], // 假设1=休假，2=外出
          leaveViewOpen: false, // 控制休假信息对话框显示
          leaveViewLoading: false, // 休假信息加载状态
          currentStaffLeaveInfo: [] // 当前员工的休假信息
    }
  },
  created() {
    this.getList()
    this.getStaffTypeList()
  },
  // computed:{
  //       // 是否显示时间段选择
  //       showTimeRange() {
  //         return this.timeRangeTags.includes(this.tagForm.staffLeave);
  //       }
  // },
  methods: {
          showTimeRange() {
            return this.timeRangeTags.includes(this.tagForm.staffLeave);
          },

    /** 查询排班人员信息列表 */
    getList() {
      this.loading = true
      listStaff(this.queryParams).then(response => {
        this.staffList = response.rows
        this.total = response.total
        this.loading = false
      })
    },

    /** 获取人员类型列表 */
    getStaffTypeList() {
      listType({ pageSize: 1000 }).then(response => {
        this.staffTypeOptions = response.rows.map(item => ({
          label: item.typeName,
          value: item.id
        }));
      });
    },

    /** 根据ID获取类型名称 */
    getStaffTypeName(id) {
      const type = this.staffTypeOptions.find(item => item.value === id);
      return type ? type.label : id;
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
        staffName: null,
        staffTypeId: null,
        staffGender: null,
        department: null,
        unit: null,
        jobTitle: null,
        staffLeave: null,
        hospitalTime: null,
        staffSort:null,
        staffDutySort:null,
        beginHospitalTime: null,
        endHospitalTime: null,
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
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        staffName: null,
        staffTypeId: null,
        staffGender: null,
        department: null,
        unit: null,
        jobTitle: null,
        hospitalTime: null,
        staffSort:null,
        staffDutySort:null,
        beginHospitalTime: null,
        endHospitalTime: null,
        status: null,
        staffLeave: null,
      }
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
      this.title = "添加排班人员信息"
    },

    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const id = row.id || this.ids
      // 确保类型列表已加载
      if (this.staffTypeOptions.length === 0) {
        this.getStaffTypeList().then(() => {
          getStaff(id).then(response => {
            this.form = response.data
            this.open = true
            this.title = "修改排班人员信息"
          })
        })
      } else {
        getStaff(id).then(response => {
          this.form = response.data
          this.open = true
          this.title = "修改排班人员信息"
        })
      }
    },

    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateStaff(this.form).then(response => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addStaff(this.form).then(response => {
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
      this.$modal.confirm('是否确认删除排班人员信息编号为"' + ids + '"的数据项？').then(function() {
        return delStaff(ids)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },

    /** 导出按钮操作 */
    handleExport() {
      this.download('staff/staff/export', {
        ...this.queryParams
      }, `staff_${new Date().getTime()}.xlsx`)
    },
    /** 标签按钮操作 */
    handleTag() {
      this.tagForm = {
        staffLeave: null,
        timeRanges: [{ timeRange: [] }]
      };
      this.tagOpen = true;
    },
        /** 标签选择变化 */
        handleTagChange(value) {
          // 如果选择了不需要时间段的标签，清空时间段
          if (!this.timeRangeTags.includes(value)) {
            this.tagForm.timeRanges = [{ timeRange: [] }];
          }
        },
         /** 添加时间段 */
            addTimeRange() {
              this.tagForm.timeRanges.push({ timeRange: [] });
            },

            /** 删除时间段 */
            removeTimeRange(index) {
              this.tagForm.timeRanges.splice(index, 1);
            },
     /** 提交标签设置 */
        submitTagForm() {
          if (!this.tagForm.staffLeave) {
            this.$modal.msgWarning("请选择标签");
            return;
          }


          // 验证时间段（如果需要）
          if (this.showTimeRange) {
            for (let i = 0; i < this.tagForm.timeRanges.length; i++) {
              const range = this.tagForm.timeRanges[i];
              if (!range.timeRange || range.timeRange.length !== 2) {
                this.$modal.msgWarning(`请完善一个时间段`);
                return;
              }
            }
          }

          // 构建时间段数据
          let timeRangeData = null;
          if (this.showTimeRange && this.tagForm.timeRanges.length > 0) {
            timeRangeData = this.tagForm.timeRanges.map(range => ({
              startTime: range.timeRange[0],
              endTime: range.timeRange[1]
            }));
          }

          // 构建更新数据
          const updateData = this.ids.map(id => ({
            id: id,
            staffLeave: this.tagForm.staffLeave,
            timeRanges: timeRangeData // 存储时间段数据
          }));

          // 调用批量更新接口
          this.loading = true;
          batchTagStaff(updateData).then(response => {
            this.$modal.msgSuccess("标签设置成功");
            this.tagOpen = false;
            this.getList();
          }).catch(() => {
            this.$modal.msgError("标签设置失败");
          }).finally(() => {
            this.loading = false;
          });
        },
    /** 取消标签按钮操作 */
    handleCancelTag() {
      if (this.ids.length === 0) {
        this.$modal.msgWarning("请选择要取消标签的人员");
        return;
      }

      this.$modal.confirm('是否确认取消选中人员的标签？').then(() => {
        this.submitCancelTag();
      }).catch(() => {});
    },

    /** 提交取消标签 */
    submitCancelTag() {
      // 构建取消标签数据
      const cancelData = this.ids.map(id => ({
        id: id,
        staffLeave: null,
        staffLeaveEndTime: null
      }));

      // 调用批量取消标签接口
      this.loading = true;
      batchCancelTag(cancelData).then(response => {
        this.$modal.msgSuccess("取消标签成功");
        this.getList();
      }).catch(() => {
        this.$modal.msgError("取消标签失败");
      }).finally(() => {
        this.loading = false;
      });
    },

    /** 查看休假按钮操作 */
    handleViewLeave() {
      if (this.ids.length === 0) {
        this.$modal.msgWarning("请选择要查看的人员");
        return;
      }

      // 从当前表格中找到选中的人员信息
      this.currentStaffLeaveInfo = this.staffList.filter(staff => this.ids.includes(staff.id));
      this.leaveViewOpen = true;
    }
  }
}
</script>
