<template>
  <div>
    <h3>个人中心</h3>
    <el-card style="margin-bottom:16px">
      <h4>个人信息</h4>
      <el-form :model="profile" label-width="100px">
        <el-form-item label="姓名"><el-input v-model="profile.realName" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="profile.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="profile.email" /></el-form-item>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </el-form>
    </el-card>
    <el-card style="margin-bottom:16px">
      <h4>购房意向</h4>
      <el-form :model="intent" label-width="100px">
        <el-form-item label="意向区域"><el-input v-model="intent.district" /></el-form-item>
        <el-form-item label="预算范围">
          <el-input-number v-model="intent.priceMin" /> ~ <el-input-number v-model="intent.priceMax" />
        </el-form-item>
        <el-form-item label="户型"><el-input v-model="intent.roomType" /></el-form-item>
        <el-button type="primary" @click="saveIntent">保存意向</el-button>
      </el-form>
    </el-card>
    <el-card>
      <h4>修改密码</h4>
      <el-form :model="pwdForm" label-width="100px">
        <el-form-item label="原密码"><el-input v-model="pwdForm.oldPassword" type="password" /></el-form-item>
        <el-form-item label="新密码"><el-input v-model="pwdForm.newPassword" type="password" /></el-form-item>
        <el-button type="primary" @click="changePassword">修改密码</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const profile = ref({ realName: '', phone: '', email: '' })
const intent = ref({ district: '', priceMin: null, priceMax: null, roomType: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '' })

onMounted(async () => {
  const res = await request.get('/customer/profile')
  profile.value = res.data
})

const saveProfile = async () => {
  await request.put('/customer/profile', profile.value)
  ElMessage.success('保存成功')
}

const saveIntent = async () => {
  await request.put('/customer/profile', { buyIntent: JSON.stringify(intent.value) })
  ElMessage.success('意向已保存')
}

const changePassword = async () => {
  await request.put('/customer/profile/password', null, { params: pwdForm.value })
  ElMessage.success('密码已修改')
  pwdForm.value = { oldPassword: '', newPassword: '' }
}
</script>
