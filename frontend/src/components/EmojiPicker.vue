<template>
  <div class="emoji-picker">
    <el-popover
      :width="320"
      trigger="click"
      :visible="visible"
      @update:visible="handleVisibleChange"
    >
      <template #reference>
        <el-button text size="small" @click="togglePicker">
          😊 表情
        </el-button>
      </template>
      
      <div class="emoji-grid">
        <div
          v-for="emoji in emojiList"
          :key="emoji.name"
          class="emoji-item"
          :title="emoji.desc"
          @click="selectEmoji(emoji)"
        >
          <span :class="['emoji-sprite', emoji.className]"></span>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script>
import { ref } from 'vue'
import { emojiConfig } from '../utils/emojiConfig.js'

export default {
  name: 'EmojiPicker',
  components: {},
  emits: ['select'],
  setup(props, { emit }) {
    const visible = ref(false)
    const emojiList = emojiConfig

    const togglePicker = () => {
      visible.value = !visible.value
    }

    const handleVisibleChange = (val) => {
      visible.value = val
    }

    const selectEmoji = (emoji) => {
      console.log('选择表情:', emoji.code)
      emit('select', emoji.code)
      visible.value = false
    }

    return {
      visible,
      emojiList,
      togglePicker,
      handleVisibleChange,
      selectEmoji
    }
  }
}
</script>

<style scoped>
.emoji-picker {
  display: inline-block;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  padding: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.emoji-item {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.emoji-item:hover {
  background-color: #f5f5f5;
}

.emoji-sprite {
  width: 24px;
  height: 24px;
  background-image: url('http://113.45.161.48:9000/admin-system/emojis/emoji-sprite.png');
  background-repeat: no-repeat;
  display: inline-block;
}

/* 表情位置定义 */
.emoji-smile { background-position: 0 0; }
.emoji-laugh { background-position: -24px 0; }
.emoji-wink { background-position: -48px 0; }
.emoji-kiss { background-position: -72px 0; }
.emoji-heart-eyes { background-position: -96px 0; }
.emoji-cool { background-position: -120px 0; }
.emoji-cry { background-position: 0 -24px; }
.emoji-angry { background-position: -24px -24px; }
.emoji-surprised { background-position: -48px -24px; }
.emoji-confused { background-position: -72px -24px; }
.emoji-shy { background-position: -96px -24px; }
.emoji-tired { background-position: -120px -24px; }
.emoji-rage { background-position: 0 -48px; }
.emoji-sick { background-position: -24px -48px; }
.emoji-thinking { background-position: -48px -48px; }
.emoji-neutral { background-position: -72px -48px; }
.emoji-tongue { background-position: -96px -48px; }
.emoji-sleep { background-position: -120px -48px; }
.emoji-thumbs-up { background-position: 0 -72px; }
.emoji-thumbs-down { background-position: -24px -72px; }
.emoji-clap { background-position: -48px -72px; }
.emoji-pray { background-position: -72px -72px; }
.emoji-victory { background-position: -96px -72px; }
.emoji-heart { background-position: -120px -72px; }
</style> 