<template>
  <div class="node" :style="{ paddingLeft: level * 14 + 'px' }">
    <div class="row">
      <button class="expander" v-if="node.children.length" @click="expanded = !expanded">
        {{ expanded ? '▾' : '▸' }}
      </button>
      <span class="expander-placeholder" v-else></span>

      <input
        v-if="showCheckbox"
        type="checkbox"
        :checked="checked"
        :indeterminate.prop="indeterminate"
        @change="toggleChecked"
      />

      <div class="title">
        <div class="name">{{ node.page.name }}</div>
        <div class="url" v-if="node.page.url">{{ node.page.url }}</div>
      </div>

      <div class="actions" v-if="showActions">
        <button class="btn-mini" @click="$emit('add-child', node.page.pageId)">添加子页面</button>
        <button class="btn-mini" @click="$emit('edit', node.page.pageId)">编辑</button>
        <button class="btn-mini danger" @click="$emit('delete', node.page.pageId)">删除</button>
      </div>
    </div>

    <div v-show="expanded" v-if="node.children.length">
      <PageTreeNode
        v-for="c in node.children"
        :key="c.page.pageId"
        :node="c"
        :level="level + 1"
        :selected-ids="selectedIds"
        :show-checkbox="showCheckbox"
        :show-actions="showActions"
        @toggle="$emit('toggle', $event)"
        @add-child="$emit('add-child', $event)"
        @edit="$emit('edit', $event)"
        @delete="$emit('delete', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { PageTreeNode } from '../utils/pageTree';
import { collectDescendantIds } from '../utils/pageTree';

defineOptions({ name: 'PageTreeNode' });

const props = defineProps<{
  node: PageTreeNode;
  level: number;
  selectedIds: number[];
  showCheckbox: boolean;
  showActions: boolean;
}>();

const emit = defineEmits<{
  (e: 'toggle', payload: { node: PageTreeNode; checked: boolean }): void;
  (e: 'add-child', pageId: number): void;
  (e: 'edit', pageId: number): void;
  (e: 'delete', pageId: number): void;
}>();

const expanded = ref(true);
const selfId = computed(() => props.node.page.pageId as number);
const descendantIds = computed(() => collectDescendantIds(props.node));
const checked = computed(() => props.selectedIds.includes(selfId.value));
const indeterminate = computed(() => {
  const selectedCount = descendantIds.value.filter(id => props.selectedIds.includes(id)).length;
  return selectedCount > 0 && selectedCount < descendantIds.value.length;
});

function toggleChecked(e: Event) {
  const target = e.target as HTMLInputElement;
  emit('toggle', { node: props.node, checked: target.checked });
}
</script>

<style scoped>
.node .row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 6px;
  border-radius: 6px;
}
.node .row:hover {
  background: #fafafa;
}
.expander,
.expander-placeholder {
  width: 22px;
  height: 22px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #666;
}
.expander-placeholder {
  cursor: default;
}
.title {
  flex: 1;
  min-width: 0;
}
.name {
  font-weight: 600;
  color: #333;
}
.url {
  font-size: 12px;
  color: #777;
  word-break: break-all;
}
.actions {
  display: flex;
  gap: 6px;
}
.btn-mini {
  border: 1px solid #ddd;
  background: #fff;
  padding: 4px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}
.btn-mini:hover {
  background: #f6f6f6;
}
.btn-mini.danger {
  border-color: #f2b8b8;
  color: #b42318;
}
</style>


