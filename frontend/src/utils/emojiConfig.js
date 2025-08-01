// 表情配置
export const emojiConfig = [
  { code: ':smile:', name: 'smile', className: 'emoji-smile', desc: '笑脸' },
  { code: ':laugh:', name: 'laugh', className: 'emoji-laugh', desc: '大笑' },
  { code: ':wink:', name: 'wink', className: 'emoji-wink', desc: '眨眼' },
  { code: ':kiss:', name: 'kiss', className: 'emoji-kiss', desc: '亲吻' },
  { code: ':heart_eyes:', name: 'heart-eyes', className: 'emoji-heart-eyes', desc: '爱心眼' },
  { code: ':cool:', name: 'cool', className: 'emoji-cool', desc: '酷' },
  { code: ':cry:', name: 'cry', className: 'emoji-cry', desc: '哭' },
  { code: ':angry:', name: 'angry', className: 'emoji-angry', desc: '生气' },
  { code: ':surprised:', name: 'surprised', className: 'emoji-surprised', desc: '惊讶' },
  { code: ':confused:', name: 'confused', className: 'emoji-confused', desc: '困惑' },
  { code: ':shy:', name: 'shy', className: 'emoji-shy', desc: '害羞' },
  { code: ':tired:', name: 'tired', className: 'emoji-tired', desc: '疲惫' },
  { code: ':rage:', name: 'rage', className: 'emoji-rage', desc: '愤怒' },
  { code: ':sick:', name: 'sick', className: 'emoji-sick', desc: '恶心' },
  { code: ':thinking:', name: 'thinking', className: 'emoji-thinking', desc: '思考' },
  { code: ':neutral:', name: 'neutral', className: 'emoji-neutral', desc: '无语' },
  { code: ':tongue:', name: 'tongue', className: 'emoji-tongue', desc: '鬼脸' },
  { code: ':sleep:', name: 'sleep', className: 'emoji-sleep', desc: '睡觉' },
  { code: ':thumbs_up:', name: 'thumbs-up', className: 'emoji-thumbs-up', desc: '拇指向上' },
  { code: ':thumbs_down:', name: 'thumbs-down', className: 'emoji-thumbs-down', desc: '拇指向下' },
  { code: ':clap:', name: 'clap', className: 'emoji-clap', desc: '掌声' },
  { code: ':pray:', name: 'pray', className: 'emoji-pray', desc: '祈祷' },
  { code: ':victory:', name: 'victory', className: 'emoji-victory', desc: '胜利' },
  { code: ':heart:', name: 'heart', className: 'emoji-heart', desc: '爱心' }
]

// 表情代码转换为显示组件
export function parseEmojis(text) {
  if (!text) return text
  
  let result = text
  emojiConfig.forEach(emoji => {
    const regex = new RegExp(emoji.code.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g')
    result = result.replace(regex, `<span class="emoji-sprite ${emoji.className}" title="${emoji.desc}"></span>`)
  })
  return result
}

// 获取表情代码列表
export function getEmojiCodes() {
  return emojiConfig.map(emoji => emoji.code)
} 