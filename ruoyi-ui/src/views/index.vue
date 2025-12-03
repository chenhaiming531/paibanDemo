<template>
  <div class="app-container">
    <div class="calendar-container">
      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button
            type="info"
            plain
            icon="el-icon-date"
            size="mini"
            @click="handleMonthlyDuty"
            v-hasPermi="['detail:detail:add']"
          >月度排班</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="success"
            plain
            icon="el-icon-edit"
            size="mini"
            @click="openDataViewDialog"
            v-hasPermi="['detail:detail:edit']"
          >手动排班</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="warning"
            plain
            icon="el-icon-download"
            size="mini"
            @click="handleExport"
            v-hasPermi="['detail:detail:export']"
          >单日导出</el-button>
        </el-col>
        <el-col :span="1.5">
          <el-button
            type="primary"
            plain
            icon="el-icon-document"
            size="mini"
            @click="handleMonthlyExport"
            v-hasPermi="['detail:detail:export']"
          >月度导出</el-button>
        </el-col>
      </el-row>

    <!-- 自定义日历单元格 -->
    <el-calendar v-model="calendarValue">
      <template slot="dateCell" slot-scope="{date, data}">
        <div
          class="cell"
          @click="handleDateClick(date)"
          @dblclick="handleDateDblClick(date)"
          :class="{ 'current-day': data.isSelected }"
        >
          <div class="day-number">{{ data.day.split('-').slice(2).join('-') }}</div>
          <!-- <div v-if="hasSchedule(date)" class="schedule-mark">已排班</div> -->
          <!-- 修改：值班人员信息并排显示，去掉逗号 -->
          <div v-if="hasDuty(date)" class="duty-info-horizontal">
            <span v-for="duty in getDutiesForDate(date)" :key="duty.id" class="duty-item-horizontal">
              <i class="el-icon-user-solid"></i>
              {{ duty.staffName }}
            </span>
          </div>
        </div>
      </template>
    </el-calendar>
    </div>

    <!-- 数据查看对话框 -->
    <el-dialog
      title="排班数据详情"
      :visible.sync="dataViewVisible"
      width="90%"
      top="5vh">
         <div>
                  <span>日期：{{ formatDate(current) }}</span>
                  <span style="margin-left: 20px;">
                    值班人员：
                    <template v-if="getDutiesForDate(current).length > 0">
                      <span v-for="(duty, index) in getDutiesForDate(current)" :key="duty.id" class="duty-tag">
                        <template v-if="index === 0">
                          <i class="el-icon-user-solid" style="color: #F56C6C;"></i>
                          一线: {{ duty.staffName }}
                          <el-button size="mini" type="text" @click="openDutyAdjustDialog(duty, 0)">调整</el-button>
                        </template>
                        <template v-else-if="index === 1">
                          <i class="el-icon-user-solid" style="color: #E6A23C;"></i>
                          二线: {{ duty.staffName }}
                          <el-button size="mini" type="text" @click="openDutyAdjustDialog(duty, 1)">调整</el-button>
                        </template>
                        <template v-else-if="index === 2 || index === 3">
                          <i class="el-icon-user-solid" style="color: #67C23A;"></i>
                          护理: {{ duty.staffName }}
                          <el-button size="mini" type="text" @click="openDutyAdjustDialog(duty, 2)">调整</el-button>
                        </template>
                        <template v-else-if="index === 4">
                          <i class="el-icon-user-solid" style="color: #409EFF;"></i>
                          进修CPB: {{ duty.staffName }}
                          <el-button size="mini" type="text" @click="openDutyAdjustDialog(duty, 4)">调整</el-button>
                        </template>
                        <template v-else-if="index === 5">
                          <i class="el-icon-user-solid" style="color: #909399;"></i>
                          进修ECMO: {{ duty.staffName }}
                          <el-button size="mini" type="text" @click="openDutyAdjustDialog(duty, 5)">调整</el-button>
                        </template>
                        <template v-else>
                          <i class="el-icon-user-solid"></i>
                          {{ duty.staffName }}
                          <el-button size="mini" type="text" @click="openDutyAdjustDialog(duty, index)">调整</el-button>
                        </template>
                      </span>
                    </template>
                    <span v-else>无</span>
                  </span>
                </div>
      <el-table
        v-loading="loading"
        :data="detailList"
        @selection-change="handleSelectionChange">
        <el-table-column label="房间名称" align="center" width="220">
          <template slot-scope="scope">
            <div style="text-align: center;">
              <div style="display: inline-flex; align-items: center; margin-bottom: 8px;">
                <i class="el-icon-s-home" style="font-size: 18px; color: #333; margin-right: 6px;"></i>
                <span style="font-weight: bold;">{{ scope.row.room.roomName }}</span>
              </div>
              <!-- 显示手术台信息 -->
              <div v-if="scope.row.staff && scope.row.staff.length > 0">
                <div
                  v-for="staff in scope.row.staff"
                  :key="staff.id"
                >
                  <div
                    v-if="staff.operatingTables && staff.operatingTables.length > 0"
                    class="operating-tables"
                  >
                    <div
                      v-for="table in staff.operatingTables"
                      :key="table.id"
                      class="operating-table-item"
                    >
                      <i class="el-icon-s-operation" style="font-size: 14px; color: #409EFF; margin-right: 4px;"></i>
                      <span>{{ table.operationName }}({{ getDifficultyLabel(table.difficulty) }}) - {{ staff.rosterStaff.staffName }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="医生名称" align="center">
          <template slot-scope="scope">
            <template v-if="scope.row.staff && scope.row.staff.length > 0">
              <span v-for="(staff, index) in scope.row.staff" :key="staff.id">
                {{ staff.rosterStaff.staffName }}
                <span v-if="index < scope.row.staff.length - 1">, </span>
              </span>
            </template>
            <span v-else>无</span>
          </template>
        </el-table-column>

        <el-table-column label="护理名称" align="center">
          <template slot-scope="scope">
            <template v-if="scope.row.nursings && scope.row.nursings.length > 0">
              <span v-for="(nursing, index) in scope.row.nursings" :key="nursing.id">
                {{ nursing.rosterStaff.staffName }}
                <span v-if="index < scope.row.nursings.length - 1">, </span>
              </span>
            </template>
            <span v-else>无</span>
          </template>
        </el-table-column>

        <el-table-column label="进修名称" align="center">
          <template slot-scope="scope">
            <template v-if="scope.row.secondaryStaff && scope.row.secondaryStaff.length > 0">
              <span v-for="(staff, index) in scope.row.secondaryStaff" :key="staff.id">
                {{ staff.rosterStaff.staffName }}
                <span v-if="index < scope.row.secondaryStaff.length - 1">, </span>
              </span>
            </template>
            <span v-else>无</span>
          </template>
        </el-table-column>

        <el-table-column label="备注" align="center" prop="adjustReason" />

        <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="text"
              icon="el-icon-edit"
              @click="handleUpdate(scope.row)"
              v-hasPermi="['detail:detail:edit']"
            >调整</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dataViewVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 调整排班对话框 -->
    <el-dialog
      title="调整排班"
      :visible.sync="adjustDialogVisible"
      width="60%"
      @close="resetAdjustForm">
      <el-form ref="adjustForm" :model="adjustForm" :rules="adjustRules" label-width="120px">
        <el-form-item label="房间" prop="roomName">
          <el-input v-model="adjustForm.roomName" disabled />
        </el-form-item>

        <!-- 手术台管理 -->
        <el-form-item label="手术台分配">
          <div class="operating-tables-management">
            <div class="add-operating-table">
              <el-select
                v-model="newOperatingTable.operationName"
                placeholder="选择手术台"
                style="width: 150px; margin-right: 10px;"
              >
                <el-option
                  v-for="option in operatingTableOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>

              <el-select
                v-model="newOperatingTable.difficulty"
                placeholder="选择手术级别"
                style="width: 150px; margin-right: 10px;"
                :disabled="!newOperatingTable.operationName"
              >
                <el-option
                  v-for="option in difficultyOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>

              <el-select
                v-model="newOperatingTable.staffId"
                placeholder="选择医生"
                filterable
                style="width: 200px; margin-right: 10px;"
                :disabled="!newOperatingTable.difficulty"
              >
                <el-option
                  v-for="staff in filteredDoctorOptions"
                  :key="staff.id"
                  :label="staff.staffName"
                  :value="staff.id"
                />
              </el-select>

              <el-button
                type="primary"
                size="small"
                @click="addOperatingTable"
                :disabled="!newOperatingTable.operationName || !newOperatingTable.difficulty || !newOperatingTable.staffId"
              >添加手术台</el-button>
            </div>

            <div class="operating-tables-list" v-if="adjustForm.operatingTables.length > 0">
              <div
                v-for="(table, index) in adjustForm.operatingTables"
                :key="index"
                class="operating-table-item"
              >
                <div class="table-info">
                  <i class="el-icon-s-operation" style="color: #409EFF; margin-right: 8px;"></i>
                  <span>{{ table.operationName }}({{ getDifficultyLabel(table.difficulty) }}) - {{ getStaffName(table.staffId) }}</span>
                </div>
                <el-button
                  type="danger"
                  icon="el-icon-delete"
                  size="mini"
                  @click="removeOperatingTable(index)"
                />
              </div>
            </div>
            <div v-else class="no-operating-tables">
              <span style="color: #909399;">暂无手术台分配</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="医生" prop="mainStaff">
          <el-select
            v-model="adjustForm.mainStaff"
            multiple
            filterable
            placeholder="请选择医生"
            style="width: 100%">
            <el-option
              v-for="staff in doctorStaffOptions"
              :key="staff.id"
              :label="staff.staffName"
              :value="staff.id">
            </el-option>
          </el-select>
        </el-form-item>

              <el-form-item label="护理" prop="nursingStaff">
                <el-select
                  v-model="adjustForm.nursingStaff"
                  multiple
                  filterable
                  placeholder="请选择护理人员"
                  style="width: 100%">
                  <el-option
                    v-for="staff in nursingStaffOptions"
                    :key="staff.id"
                    :label="staff.staffName"
                    :value="staff.id">
                  </el-option>
                </el-select>
              </el-form-item>

        <el-form-item label="进修" prop="secondaryStaff">
          <el-select
            v-model="adjustForm.secondaryStaff"
            multiple
            filterable
            placeholder="请选择进修"
            style="width: 100%">
            <el-option
              v-for="staff in secondaryStaffOptions"
              :key="staff.id"
              :label="staff.staffName"
              :value="staff.id">
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="备注" prop="adjustReason">
          <el-input
            v-model="adjustForm.adjustReason"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="adjustDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitAdjustForm">确 定</el-button>
      </div>
    </el-dialog>
    <!-- 调整值班人员对话框 -->
        <el-dialog
          title="调整值班人员"
          :visible.sync="dutyAdjustDialogVisible"
          width="30%"
          @close="resetDutyAdjustForm">
          <el-form ref="dutyAdjustForm" :model="dutyAdjustForm" label-width="100px">
            <el-form-item label="当前人员">
              <el-input v-model="dutyAdjustForm.currentStaffName" disabled />
            </el-form-item>

            <el-form-item label="选择人员" prop="newStaffId">
              <el-select
                v-model="dutyAdjustForm.newStaffId"
                filterable
                placeholder="请选择值班人员"
                style="width: 100%">
                <el-option
                  v-for="staff in filteredDutyStaffOptions"
                  :key="staff.id"
                  :label="staff.staffName"
                  :value="staff.id">
                </el-option>
              </el-select>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button @click="dutyAdjustDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="submitDutyAdjustForm">确 定</el-button>
          </div>
        </el-dialog>
  </div>
</template>

<script>
import { intelligent, view, getStaffList, updateDetail, distinctDates ,monthlyDuty ,listRosterDuty ,updateRosterDuty ,monthlyExport} from "@/api/detail/detail"
import { listStaff } from "@/api/staff/staff"

export default {
  dicts: ['sys_yes_no2'],
  data() {
    return {
            // 新增数据
            dutyAdjustDialogVisible: false,
            dutyAdjustForm: {
              dutyId: null,
              positionIndex: null,
              currentStaffId: null,
              currentStaffName: '',
              newStaffId: null
            },
      dutyList: [],
      calendarValue: new Date(),
      current: new Date(),
      detailList: [],
      loading: false,
      dataViewVisible: false,
      adjustDialogVisible: false,
      selectedRows: [],
      staffOptions: [],
      doctorStaffOptions: [], // 医生类型人员
       nursingStaffOptions: [], // 护理类型人员（新增）
      secondaryStaffOptions: [], // 进修类型人员
      Dates:[],

      // 调整表单
      adjustForm: {
        id: null,
        roomName: '',
        operatingTables: [],
        mainStaff: [],
         nursingStaff: [], // 新增护理人员字段
        secondaryStaff: [],
        shiftType: '',
        adjustReason: ''
      },

      // 新手术台表单
      newOperatingTable: {
        operationName: '',
        difficulty: '',
        staffId: null
      },

      // 手术台选项
      operatingTableOptions: [
        { value: '第一手术台', label: '第一手术台' },
        { value: '第二手术台', label: '第二手术台' },
        { value: '第三手术台', label: '第三手术台' },
        { value: '第四手术台', label: '第四手术台' },
        { value: '第五手术台', label: '第五手术台' },
        { value: '第六手术台', label: '第六手术台' }
      ],

      // 手术难度选项 - 修改为数字类型
      difficultyOptions: [
        { value: 1, label: '初级' },
        { value: 2, label: '中级' },
        { value: 3, label: '高级' }
      ],

      list1: [],

      // 验证规则
      adjustRules: {
        // mainStaff: [
        //   { required: true, message: '请至少选择一个主班人员', trigger: 'blur' }
        // ]
      }
    }
  },

  created() {
    this.loadScheduleDates()
    this.getStaffOptions()
    this.loadDutyData() // 新增：加载值班数据
  },
  computed: {
     // 根据职位索引过滤值班人员选项
        filteredDutyStaffOptions() {
          const positionIndex = this.dutyAdjustForm.positionIndex;

          if (positionIndex === 0) {
            // 一线：只能选择6L人员
            return this.staffOptions.filter(staff => staff.staffTypeId === 6);
          } else if (positionIndex === 1) {
            // 二线：只能选择5L人员
            return this.staffOptions.filter(staff => staff.staffTypeId === 5);
          } else if (positionIndex === 2 || positionIndex === 3) {
            // 护理：可以选择13L人员
            return this.staffOptions.filter(staff => staff.staffTypeId === 13);
          } else if (positionIndex === 4) {
            // 进修CPB：只能选择8L人员
            return this.staffOptions.filter(staff => staff.staffTypeId === 8);
          } else if (positionIndex === 5) {
            // 进修ECMO：只能选择10L人员
            return this.staffOptions.filter(staff => staff.staffTypeId === 10);
          }

          // 默认返回空数组
          return [];
        },
    // 根据选择的难度筛选医生
    filteredDoctorOptions() {
      // 如果没有选择难度，不显示任何医生
      if (!this.newOperatingTable.difficulty) {
        return []
      }

      // 初级难度，所有医生都可以
      if (this.newOperatingTable.difficulty === 1) {
        return this.doctorStaffOptions
      }

      // 中、高级难度，只显示职称包含'主任'的医生
      return this.doctorStaffOptions.filter(staff =>
        staff.jobTitle && staff.jobTitle.includes('主任')
      )
    }
  },
  methods: {
      /** 月度导出按钮操作 */
      handleMonthlyExport() {
        const formattedDate = this.formatDate(this.current)
        this.download('/detail/detail/monthlyExport', {
          date: formattedDate
        }, `月度值班表_${formattedDate.substring(0, 7)}.docx`)
      },
     // 打开调整值班人员对话框
        openDutyAdjustDialog(duty, positionIndex) {
          this.dutyAdjustForm.dutyId = duty.id;
          this.dutyAdjustForm.positionIndex = positionIndex;
          this.dutyAdjustForm.currentStaffId = duty.staffId; // 假设duty对象中有staffId
          this.dutyAdjustForm.currentStaffName = duty.staffName;
          this.dutyAdjustForm.newStaffId = null;

          this.dutyAdjustDialogVisible = true;
        },

        // 提交值班人员调整
        submitDutyAdjustForm() {
          if (!this.dutyAdjustForm.newStaffId) {
            this.$modal.msgWarning("请选择新的值班人员");
            return;
          }

          const formData = {
            id: this.dutyAdjustForm.dutyId,
            dutyStaffId: this.dutyAdjustForm.newStaffId
          };

          updateRosterDuty(formData).then(response => {
            this.$modal.msgSuccess("值班人员调整成功");
            this.dutyAdjustDialogVisible = false;
            this.loadDutyData(); // 重新加载值班数据
          }).catch(error => {
            this.$modal.msgError("值班人员调整失败");
            console.error("调整值班人员错误:", error);
          });
        },

        // 重置值班调整表单
        resetDutyAdjustForm() {
          this.dutyAdjustForm = {
            dutyId: null,
            positionIndex: null,
            currentStaffId: null,
            currentStaffName: '',
            newStaffId: null
          };
        },
    // 新增：加载值班数据
        loadDutyData() {
          listRosterDuty().then(response => {
            this.dutyList = response.rows || []
          }).catch(error => {
            console.error("获取值班数据失败:", error)
            this.dutyList = []
          })
        },

        // 新增：检查指定日期是否有值班
        hasDuty(date) {
          if (!this.dutyList || this.dutyList.length === 0) return false
          const dateStr = this.formatDate(date)
          return this.dutyList.some(duty => this.formatDate(duty.dutyTime) === dateStr)
        },

        // 新增：获取指定日期的值班信息
        getDutiesForDate(date) {
          if (!this.dutyList || this.dutyList.length === 0) return []
          const dateStr = this.formatDate(date)

          return this.dutyList
            .filter(duty => this.formatDate(duty.dutyTime) === dateStr)
            .map(duty => ({
              id: duty.id,
              staffName: duty.staff ? duty.staff.staffName : '未知人员'
            }))
        },
    handleMonthlyDuty() {
      const formattedDate = this.formatDate(this.current);

      // 创建符合后端要求的请求数据格式
      const requestData = {
        date: formattedDate
      };

      this.$modal.loading("正在执行月度排班，请稍候...");

      monthlyDuty(requestData).then(response => {
        this.$modal.closeLoading();
        this.$modal.msgSuccess(response.msg || "月度排班成功");
        this.loadScheduleDates();
         // 排班成功后重新加载数据
        this.loadScheduleDates(); // 重新加载排班日期
        this.loadDutyData();      // 重新加载值班数据
      }).catch(error => {
        this.$modal.closeLoading();
        const errorMsg = error.response?.data?.msg || error.message || "月度排班失败";
        this.$modal.msgError(errorMsg);
        console.error("月度排班错误:", error);
      });
    },
        // 添加双击日期处理方法
        handleDateDblClick(date) {
          this.current = new Date(date);
          this.calendarValue = new Date(date);
          this.openDataViewDialog(); // 直接打开详细页面对话框
        },
    // 获取难度标签文本
    getDifficultyLabel(difficulty) {
      // 确保 difficulty 是数字类型
      const diffNum = Number(difficulty) || 0;

      // 定义明确的映射关系
      const difficultyMap = {
        1: '初级',
        2: '中级',
        3: '高级'
      };

      return difficultyMap[diffNum] || '未知';
    },

    // 加载有排班的日期
    loadScheduleDates() {
      distinctDates().then(response => {
        let dates = []
        if (Array.isArray(response.data)) {
          dates = response.data
        } else if (Array.isArray(response.rows)) {
          dates = response.rows
        } else if (Array.isArray(response)) {
          dates = response
        }

        this.Dates = dates
          .map(dateStr => {
            if (!dateStr) return null
            const date = new Date(dateStr);
            if (isNaN(date.getTime())) return null
            return this.formatDate(date)
          })
          .filter(date => date)

      }).catch(error => {
        console.error("获取排班日期失败:", error)
        this.Dates = []
      })
    },

    // 检查指定日期是否有排班
    // hasSchedule(date) {
    //   if (!this.Dates || this.Dates.length === 0) return false
    //   const dateStr = this.formatDate(date)
    //   return this.Dates.includes(dateStr)
    // },

    formatDate(date) {
      const d = date instanceof Date ? date : new Date(date)
      const year = d.getFullYear()
      const month = (d.getMonth() + 1).toString().padStart(2, '0')
      const day = d.getDate().toString().padStart(2, '0')
      return `${year}-${month}-${day}`
    },

    handleDateClick(date) {
      this.current = new Date(date)
      this.calendarValue = new Date(date)
    },

    intelligent() {
      intelligent(this.current).then(response => {
        this.$modal.msgSuccess(response.msg)
        this.loadScheduleDates()
      })
    },

    openDataViewDialog() {
      this.loading = true
      this.dataViewVisible = true

      view(this.current).then(response => {
        this.detailList = response.rows
        this.loading = false
      }).catch(() => {
        this.loading = false
      })
    },

    handleSelectionChange(rows) {
      this.selectedRows = rows
    },

    // 获取人员选项
    getStaffOptions() {
      listStaff({ pageSize: 100 }).then(response => {
        this.staffOptions = response.rows
        // 分离医生（5L和6L类型）和进修人员（8L类型）
        this.doctorStaffOptions = response.rows.filter(staff =>
          staff.staffTypeId === 5 || staff.staffTypeId === 6 || staff.staffTypeId === 14 || staff.staffTypeId === 12
        )
                this.nursingStaffOptions = response.rows.filter(staff =>
                  staff.staffTypeId === 7 || staff.staffTypeId === 13
                )
        this.secondaryStaffOptions = response.rows.filter(staff => staff.staffTypeId === 8 ||
        staff.staffTypeId === 10 )
      })
    },

    // 根据staffId获取人员名称
    getStaffName(staffId) {
      const staff = this.staffOptions.find(s => s.id === staffId)
      return staff ? staff.staffName : '未知'
    },

    // 打开调整对话框
// 打开调整对话框
handleUpdate(row) {
  this.list1 = row
  this.adjustForm.id = row.id
  this.adjustForm.roomName = row.room.roomName
  this.adjustForm.shiftType = row.shiftType
  this.adjustForm.adjustReason = row.adjustReason || ''

  // 设置主班人员
  if (row.staff && row.staff.length > 0) {
    this.adjustForm.mainStaff = row.staff.map(item => item.rosterStaff.id)
  } else {
    this.adjustForm.mainStaff = []
  }

  // 新增：设置护理人员
  if (row.nursings && row.nursings.length > 0) {
    this.adjustForm.nursingStaff = row.nursings.map(item => item.rosterStaff.id)
  } else {
    this.adjustForm.nursingStaff = []
  }

  // 设置次班人员
  if (row.secondaryStaff && row.secondaryStaff.length > 0) {
    this.adjustForm.secondaryStaff = row.secondaryStaff.map(item => item.rosterStaff.id)
  } else {
    this.adjustForm.secondaryStaff = []
  }

  // 设置手术台数据 - 修复：确保使用正确的staffId
  this.adjustForm.operatingTables = []
  if (row.staff) {
    row.staff.forEach(staff => {
      if (staff.operatingTables && staff.operatingTables.length > 0) {
        staff.operatingTables.forEach(table => {
          this.adjustForm.operatingTables.push({
            operationName: table.operationName,
            difficulty: table.difficulty,
            staffId: staff.rosterStaff.id, // 使用rosterStaff的id而不是relation的id
            relationId: staff.id
          })
        })
      }
    })
  }

  this.adjustDialogVisible = true
},

    // 添加手术台
// 添加手术台
// 添加手术台
addOperatingTable() {
  if (this.newOperatingTable.operationName &&
      this.newOperatingTable.difficulty &&
      this.newOperatingTable.staffId) {

    // 检查是否已经为该医生分配了该手术台
    const existingTable = this.adjustForm.operatingTables.find(
      table => table.operationName === this.newOperatingTable.operationName &&
              table.difficulty === this.newOperatingTable.difficulty &&
              table.staffId === this.newOperatingTable.staffId
    )

    if (existingTable) {
      this.$modal.msgWarning("该医生已经分配了这个手术台和难度的组合")
      return
    }

    // 确保选择的医生也在主班人员中（使用严格比较避免重复）
    const staffId = this.newOperatingTable.staffId
    if (!this.adjustForm.mainStaff.some(id => id === staffId)) {
      // 这里需要确保医生被添加到主班人员列表中
      this.adjustForm.mainStaff.push(staffId)

      // 强制更新视图，确保医生名称立即显示
      this.$nextTick(() => {
        this.addTableToForm(staffId)
      })
    } else {
      // 如果医生已经在主班人员中，直接添加手术台
      this.addTableToForm(staffId)
    }
  }
},

// 新增辅助方法：将手术台添加到表单
addTableToForm(staffId) {
  this.adjustForm.operatingTables.push({
    operationName: this.newOperatingTable.operationName,
    difficulty: this.newOperatingTable.difficulty,
    staffId: staffId
  })

  // 清空表单
  this.newOperatingTable.operationName = ''
  this.newOperatingTable.difficulty = ''
  this.newOperatingTable.staffId = null

  // 强制更新视图
  this.$forceUpdate();
},

    // 删除手术台
// 删除手术台
removeOperatingTable(index) {
  const tableToRemove = this.adjustForm.operatingTables[index];

  // 检查是否还有其他手术台使用同一个医生
  const hasOtherTablesForSameDoctor = this.adjustForm.operatingTables.some(
    (table, i) => i !== index && table.staffId === tableToRemove.staffId
  );

  // 如果没有其他手术台使用这个医生，且医生不在主班人员列表中，则从主班人员中移除
  if (!hasOtherTablesForSameDoctor && this.adjustForm.mainStaff.includes(tableToRemove.staffId)) {
    // 检查这个医生是否原本就在主班人员中（不是通过手术台添加的）
    const wasOriginalDoctor = this.list1.staff && this.list1.staff.some(
      staff => staff.rosterStaff.id === tableToRemove.staffId
    );

    // 如果不是原本的主班医生，就从主班人员中移除
    if (!wasOriginalDoctor) {
      this.adjustForm.mainStaff = this.adjustForm.mainStaff.filter(
        id => id !== tableToRemove.staffId
      );
    }
  }

  // 删除手术台
  this.adjustForm.operatingTables.splice(index, 1);
},

    // 提交调整表单
    submitAdjustForm() {
      this.$refs.adjustForm.validate(valid => {
        if (valid) {
          // 确保手术台选择的医生也在主班人员中（去重处理）
          const operatingTableStaffIds = this.adjustForm.operatingTables
            .map(table => table.staffId)
            .filter((staffId, index, array) => array.indexOf(staffId) === index);

          // 合并主班人员和手术台选择的医生（确保不重复）
          const allMainStaffIds = [
            ...new Set([...this.adjustForm.mainStaff, ...operatingTableStaffIds])
          ];

          // 构建手术台数据，需要包含 relationId
          const operatingTablesWithRelation = this.adjustForm.operatingTables.map(table => {
            // 查找对应的 relationId（如果已有）
            const staffRelation = this.list1.staff && this.list1.staff.find(
              s => s.rosterStaff.id === table.staffId
            );

            return {
              operationName: table.operationName,
              difficulty: table.difficulty,
              staffId: table.staffId,
              relationId: staffRelation ? staffRelation.id : null
            };
          });

          const formData = {
            id: this.adjustForm.id,
            mainStaffIds: allMainStaffIds,
            nursingStaffIds: this.adjustForm.nursingStaff, // 新增护理人员数据
            secondaryStaffIds: this.adjustForm.secondaryStaff,
            shiftType: this.adjustForm.shiftType,
            adjustReason: this.adjustForm.adjustReason,
            isAdjusted: '1',
            operatingTables: operatingTablesWithRelation
          };

          updateDetail(formData).then(response => {
            this.$modal.msgSuccess("调整成功")
            this.adjustDialogVisible = false
            this.loadScheduleDates()
            this.openDataViewDialog()
          }).catch(error => {
            this.$modal.msgError("调整失败")
          })
        }
      })
    },
    // 重置调整表单
    resetAdjustForm() {
      this.adjustForm = {
        id: null,
        roomName: '',
        operatingTables: [],
        mainStaff: [],
         nursingStaff: [], // 重置护理人员字段
        secondaryStaff: [],
        shiftType: '',
        adjustReason: ''
      }
      this.newOperatingTable = {
        operationName: '',
        difficulty: '',
        staffId: null
      }
      this.$refs.adjustForm.clearValidate()
    },

    /** 导出按钮操作 */
    handleExport() {
      const formattedDate = this.formatDate(this.current)
      this.download('/detail/detail/export', {
        date: formattedDate
      }, `排班数据_${formattedDate}.docx`)
    }
  }
}
</script>

<style scoped>
/* 样式保持不变 */
.calendar-container {
  margin-bottom: 20px;
  background: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.cell {
  height: 100%;
  padding: 5px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
   position: relative;
}

.cell::after {
  content: "双击查看详情";
  position: absolute;
  bottom: 2px;
  left: 0;
  right: 0;
  font-size: 9px;
  color: #909399;
  opacity: 0;
  transition: opacity 0.3s;
  pointer-events: none;
}

.cell:hover::after {
  opacity: 1;
}

.cell:hover {
  background-color: #f5f7fa;
}

.current-day {
  background-color: #e6f7ff;
  color: #1890ff;
  border-radius: 4px;
}

.day-number {
  font-size: 8px;
    font-weight: bold;
}

.schedule-mark {
  font-size: 12px;
  color: #67c23a;
  background-color: #f0f9eb;
  border-radius: 4px;
  padding: 2px 4px;
  margin-top: 4px;
}

/* 手术台样式 */
.operating-tables {
  margin-top: 8px;
}

.operating-table-item {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
  padding: 4px;
  background: #f5f7fa;
  border-radius: 3px;
  font-size: 12px;
}

.no-operating-tables {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}

/* 手术台管理样式 */
.operating-tables-management {
  border: 1px solid #DCDFE6;
  border-radius: 4px;
  padding: 15px;
  background: #F5F7FA;
}

.add-operating-table {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
  flex-wrap: wrap;
  gap: 10px;
}

.operating-tables-list {
  margin-top: 10px;
}

.operating-table-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  margin-bottom: 8px;
  background: white;
  border-radius: 4px;
  border: 1px solid #EBEEF5;
}

.table-info {
  display: flex;
  align-items: center;
}

/* 对话框表格样式 */
.el-dialog__body {
  padding: 20px;
}

/* 按钮间距 */
.mb8 .el-col {
  margin-right: 8px;
}

/* 添加表格样式优化 */
.el-table .cell {
  white-space: pre-line;
}

/* 新增：值班信息样式 */
.duty-info {
  margin-top: 4px;
  font-size: 11px;
}

.duty-item {
  display: flex;
  align-items: center;
  margin-bottom: 2px;
  color: #E6A23C;
  background-color: #FDF6EC;
  border-radius: 3px;
  padding: 2px 4px;
}

.duty-item i {
  font-size: 10px;
  margin-right: 3px;
}

/* 调整单元格高度以适应额外内容 */
.cell {
  min-height: 80px;
  height: auto;
}

/* 确保在有值班信息时，排班标记仍然可见 */
.schedule-mark {
  margin-top: 2px;
  margin-bottom: 2px;
}
/* 水平排列的值班信息样式 - 修改为每行最多两个 */
.duty-info-horizontal {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 2px;
  margin-top: 4px;
  font-size: 11px;
  line-height: 1.2;
  width: 100%;
}

.duty-item-horizontal {
  display: flex;
  align-items: center;
  color: #E6A23C;
  background-color: #FDF6EC;
  border-radius: 3px;
  padding: 2px 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  justify-content: flex-start;
  min-width: 0;
}

.duty-item-horizontal i {
  font-size: 10px;
  margin-right: 3px;
}



/* 确保在有值班信息时，排班标记仍然可见 */
.schedule-mark {
  margin-top: 2px;
  margin-bottom: 2px;
}
/* 添加调整按钮样式 */
.duty-tag .el-button {
  margin-left: 5px;
  padding: 0 5px;
  height: 20px;
  line-height: 20px;
}
</style>
