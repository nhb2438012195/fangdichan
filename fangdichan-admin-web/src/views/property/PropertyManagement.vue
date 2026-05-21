<template>
  <div>
    <div style="margin-bottom:16px">
      <el-button type="primary" @click="showDialog = true">发布房源</el-button>
    </div>
    <el-table :data="properties" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="district" label="区域" />
      <el-table-column prop="price" label="价格" />
      <el-table-column prop="status" label="状态" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button @click="editProperty(row)">编辑</el-button>
          <el-button type="danger" @click="takeOff(row.id)" v-if="row.status === 'APPROVED'">下架</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="showDialog" :title="editingId ? '编辑房源' : '发布房源'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="区域"><el-input v-model="form.district" /></el-form-item>
        <el-form-item label="地址"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="楼层区间"><el-input v-model="form.floor" /></el-form-item>
        <el-form-item label="总楼层"><el-input type="number" v-model="form.floorTotal" /></el-form-item>
        <el-form-item label="户型"><el-input v-model="form.roomType" /></el-form-item>
        <el-form-item label="面积(㎡)"><el-input type="number" v-model="form.area" /></el-form-item>
        <el-form-item label="价格(元)"><el-input type="number" v-model="form.price" /></el-form-item>
        <el-form-item label="描述"><el-input type="textarea" v-model="form.description" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProperty">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const properties = ref([])
const showDialog = ref(false)
const editingId = ref(null)
const form = ref({ title: '', district: '', location: '', floor: '', floorTotal: '', roomType: '', area: '', price: '', description: '' })

const fetchList = async () => {
  const res = await request.get('/agent/property/list', { params: { page: 1, size: 100 } })
  properties.value = res.data.list
}

const saveProperty = async () => {
  if (editingId.value) {
    await request.put(`/agent/property/${editingId.value}`, form.value)
  } else {
    await request.post('/agent/property', form.value)
  }
  ElMessage.success('保存成功')
  showDialog.value = false
  fetchList()
}

const editProperty = (row) => {
  editingId.value = row.id
  form.value = { ...row }
  showDialog.value = true
}

const takeOff = async (id) => {
  await request.put(`/agent/property/${id}/off-market`)
  ElMessage.success('已下架')
  fetchList()
}

onMounted(fetchList)
</script>
