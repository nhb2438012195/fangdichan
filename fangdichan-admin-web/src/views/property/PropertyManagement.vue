<template>
  <div>
    <div style="margin-bottom: 16px">
      <el-button type="primary" @click="openCreate">发布房源</el-button>
    </div>
    <el-table v-loading="loading" :data="properties" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="district" label="区域" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="editProperty(row)">编辑</el-button>
          <el-button v-if="row.status === 'APPROVED'" type="danger" @click="handleTakeOff(row.id)"
            >下架</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑房源' : '发布房源'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="区域"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="楼层区间"><el-input v-model="form.floor" /></el-form-item>
        <el-form-item label="总楼层"
          ><el-input v-model="form.floorTotal" type="number"
        /></el-form-item>
        <el-form-item label="户型"><el-input v-model="form.roomType" /></el-form-item>
        <el-form-item label="面积(㎡)"><el-input v-model="form.area" type="number" /></el-form-item>
        <el-form-item label="价格(元)"
          ><el-input v-model="form.price" type="number"
        /></el-form-item>
        <el-form-item label="描述"
          ><el-input v-model="form.description" type="textarea"
        /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveProperty">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getMyPropertyList,
  createProperty,
  updateProperty,
  takeOffProperty
} from '../../api/agent-property'

const properties = ref([])
const showDialog = ref(false)
const editingId = ref(null)
const loading = ref(false)
const saving = ref(false)

const emptyForm = () => ({
  title: '',
  district: '',
  location: '',
  floor: '',
  floorTotal: '',
  roomType: '',
  area: '',
  price: '',
  description: ''
})
const form = ref(emptyForm())

const fetchList = async () => {
  loading.value = true
  try {
    const result = await getMyPropertyList({ page: 1, size: 100 })
    properties.value = result.list || []
  } catch {
    properties.value = []
  } finally {
    loading.value = false
  }
}

const openCreate = () => {
  editingId.value = null
  form.value = emptyForm()
  showDialog.value = true
}

const saveProperty = async () => {
  saving.value = true
  try {
    if (editingId.value) {
      await updateProperty(editingId.value, form.value)
    } else {
      await createProperty(form.value)
    }
    ElMessage.success('保存成功')
    showDialog.value = false
    fetchList()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const editProperty = (row) => {
  editingId.value = row.id
  form.value = JSON.parse(JSON.stringify(row))
  showDialog.value = true
}

const handleTakeOff = async (id) => {
  try {
    await takeOffProperty(id)
    ElMessage.success('已下架')
    fetchList()
  } catch {
    ElMessage.error('下架失败')
  }
}

onMounted(fetchList)
</script>
