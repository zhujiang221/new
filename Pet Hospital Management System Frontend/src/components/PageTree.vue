<template>
  <div class="page-tree">
    <PageTreeNode
      v-for="n in nodes"
      :key="n.page.pageId"
      :node="n"
      :level="0"
      :selected-ids="selectedIds"
      :show-checkbox="showCheckbox"
      :show-actions="showActions"
      @toggle="onToggle"
      @add-child="$emit('add-child', $event)"
      @edit="$emit('edit', $event)"
      @delete="$emit('delete', $event)"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { PageTreeNode as PageTreeNodeType } from '../utils/pageTree';
import { collectDescendantIds } from '../utils/pageTree';
import PageTreeNode from './PageTreeNode.vue';

const props = defineProps<{
  nodes: PageTreeNodeType[];
  modelValue?: number[];
  showCheckbox?: boolean;
  showActions?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', v: number[]): void;
  (e: 'add-child', pageId: number): void;
  (e: 'edit', pageId: number): void;
  (e: 'delete', pageId: number): void;
}>();

const selectedIds = computed(() => props.modelValue || []);
const showCheckbox = computed(() => props.showCheckbox !== false);
const showActions = computed(() => props.showActions === true);

function setSelected(next: Set<number>) {
  emit('update:modelValue', Array.from(next));
}

function onToggle(payload: { node: PageTreeNodeType; checked: boolean }) {
  const ids = new Set(selectedIds.value);
  const all = collectDescendantIds(payload.node);
  if (payload.checked) {
    all.forEach(id => ids.add(id));
  } else {
    all.forEach(id => ids.delete(id));
  }
  setSelected(ids);
}
</script>

<style scoped>
.page-tree {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 8px;
}
</style>


