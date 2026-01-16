<template>
  <div class="image-uploader">
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      @change="handleFileSelect"
      style="display: none"
    />
    <div v-if="preview" class="image-preview">
      <img :src="preview" alt="预览" />
      <button class="remove-btn" @click="removeImage">×</button>
    </div>
    <button v-else class="upload-btn" @click="triggerFileInput">
      <span class="upload-icon">📷</span>
      <span class="upload-text">选择图片</span>
    </button>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

const props = defineProps<{
  maxSize?: number; // MB
}>();

const emit = defineEmits<{
  (e: 'upload', file: File): void;
  (e: 'remove'): void;
}>();

const fileInput = ref<HTMLInputElement | null>(null);
const preview = ref<string>('');

const maxSize = props.maxSize || 5; // 默认5MB

function triggerFileInput() {
  fileInput.value?.click();
}

function handleFileSelect(event: Event) {
  const target = event.target as HTMLInputElement;
  const file = target.files?.[0];
  if (!file) return;

  // 检查文件类型
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件');
    return;
  }

  // 检查文件大小
  if (file.size > maxSize * 1024 * 1024) {
    alert(`图片大小不能超过 ${maxSize}MB`);
    return;
  }

  // 创建预览
  const reader = new FileReader();
  reader.onload = (e) => {
    preview.value = e.target?.result as string;
  };
  reader.readAsDataURL(file);

  // 触发上传事件
  emit('upload', file);
}

function removeImage() {
  preview.value = '';
  if (fileInput.value) {
    fileInput.value.value = '';
  }
  emit('remove');
}
</script>

<style scoped>
.image-uploader {
  display: inline-block;
}

.upload-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: #f0f0f0;
  border: 1px dashed #ccc;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #666;
}

.upload-btn:hover {
  background: #e0e0e0;
  border-color: #72C1BB;
  color: #72C1BB;
}

.upload-icon {
  font-size: 18px;
}

.upload-text {
  font-size: 14px;
}

.image-preview {
  position: relative;
  display: inline-block;
}

.image-preview img {
  max-width: 200px;
  max-height: 200px;
  border-radius: 8px;
  object-fit: cover;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.remove-btn:hover {
  background: #ff7875;
  transform: scale(1.1);
}

/* 移动端适配 */
@media (max-width: 767px) {
  .upload-btn {
    padding: 6px 12px;
    font-size: 13px;
  }

  .image-preview img {
    max-width: 150px;
    max-height: 150px;
  }
}
</style>
