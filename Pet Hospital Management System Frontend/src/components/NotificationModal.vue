<template>
  <el-dialog
    v-model="visible"
    title="新消息提醒"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="true"
    @close="handleClose"
  >
    <div class="notification-modal-content">
      <div class="message-icon">🔔</div>
      <div class="message-text">{{ message }}</div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">稍后查看</el-button>
        <el-button type="primary" @click="handleViewDetail">查看详情</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';

interface Props {
  modelValue: boolean;
  message: string;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  message: '您有新消息'
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
  'close': [];
}>();

const router = useRouter();
const visible = ref(props.modelValue);

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal;
});

watch(visible, (newVal) => {
  emit('update:modelValue', newVal);
});

function handleClose() {
  visible.value = false;
  emit('close');
}

function handleViewDetail() {
  visible.value = false;
  // 跳转到消息页面
  router.push('/doctor/message');
}
</script>

<style scoped>
.notification-modal-content {
  text-align: center;
  padding: 20px 0;
}

.message-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.message-text {
  font-size: 16px;
  color: #333;
  line-height: 1.6;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
