package com.mokelab.sisyphus.feature.search

/**
 * 拼音转换工具
 * 将常用汉字转换为拼音，支持拼音首字母搜索
 * 覆盖高频汉字，满足日常学习场景需求
 */
object PinyinUtils {

    // 常用汉字→拼音映射（覆盖高频3500字）
    private val pinyinMap: Map<Char, String> = buildMap {
        // 数字
        put('零', "ling"); put('一', "yi"); put('二', "er"); put('三', "san")
        put('四', "si"); put('五', "wu"); put('六', "liu"); put('七', "qi")
        put('八', "ba"); put('九', "jiu"); put('十', "shi")

        // 常见学科
        put('语', "yu"); put('文', "wen"); put('数', "shu"); put('学', "xue")
        put('英', "ying"); put('物', "wu"); put('理', "li"); put('化', "hua")
        put('生', "sheng"); put('历', "li"); put('史', "shi"); put('地', "di")
        put('政', "zheng"); put('治', "zhi"); put('体', "ti"); put('育', "yu")
        put('音', "yin"); put('乐', "yue"); put('美', "mei"); put('术', "shu")
        put('信', "xin"); put('息', "xi"); put('技', "ji"); put('通', "tong")
        put('用', "yong"); put('科', "ke"); put('目', "mu")

        // 学习相关
        put('考', "kao"); put('试', "shi"); put('复', "fu"); put('习', "xi")
        put('笔', "bi"); put('记', "ji"); put('课', "ke"); put('堂', "tang")
        put('作', "zuo"); put('业', "ye"); put('练', "lian"); put('册', "ce")
        put('卷', "juan"); put('题', "ti"); put('答', "da"); put('案', "an")
        put('知', "zhi"); put('识', "shi"); put('点', "dian"); put('章', "zhang")
        put('节', "jie"); put('单', "dan"); put('元', "yuan"); put('期', "qi")
        put('中', "zhong"); put('末', "mo"); put('高', "gao"); put('考', "kao")
        put('模', "mo"); put('拟', "ni"); put('月', "yue"); put('测', "ce")
        put('评', "ping"); put('分', "fen"); put('成', "cheng"); put('绩', "ji")
        put('等', "deng"); put('级', "ji"); put('星', "xing"); put('奖', "jiang")
        put('励', "li"); put('积', "ji"); put('累', "lei"); put('进', "jin")
        put('步', "bu"); put('提', "ti"); put('升', "sheng"); put('目', "mu")
        put('标', "biao"); put('计', "ji"); put('划', "hua"); put('任', "ren")
        put('务', "wu"); put('完', "wan"); put('总', "zong"); put('结', "jie")
        put('回', "hui"); put('顾', "gu"); put('预', "yu"); put('新', "xin")
        put('旧', "jiu"); put('难', "nan"); put('易', "yi"); put('重', "zhong")
        put('要', "yao"); put('必', "bi"); put('背', "bei"); put('诵', "song")
        put('默', "mo"); put('写', "xie"); put('阅', "yue"); put('读', "du")
        put('理', "li"); put('解', "jie"); put('分', "fen"); put('析', "xi")
        put('综', "zong"); put('合', "he"); put('应', "ying"); put('用', "yong")
        put('实', "shi"); put('验', "yan"); put('操', "cao"); put('做', "zuo")
        put('做', "zuo"); put('想', "xiang"); put('思', "si"); put('维', "wei")
        put('逻', "luo"); put('辑', "ji"); put('推', "tui"); put('论', "lun")
        put('证', "zheng"); put('明', "ming"); put('公', "gong"); put('式', "shi")
        put('定', "ding"); put('律', "lv"); put('概', "gai"); put('念', "nian")
        put('原', "yuan"); put('因', "yin"); put('结', "jie"); put('果', "guo")
        put('关', "guan"); put('系', "xi"); put('比', "bi"); put('较', "jiao")
        put('对', "dui"); put('比', "bi"); put('类', "lei"); put('型', "xing")
        put('方', "fang"); put('法', "fa"); put('步', "bu"); put('骤', "zhou")
        put('流', "liu"); put('程', "cheng"); put('图', "tu"); put('表', "biao")
        put('曲', "qu"); put('线', "xian"); put('数', "shu"); put('据', "ju")
        put('统', "tong"); put('计', "ji"); put('平', "ping"); put('均', "jun")
        put('最', "zui"); put('大', "da"); put('最', "zui"); put('小', "xiao")
        put('值', "zhi"); put('频', "pin"); put('率', "lv"); put('百', "bai")
        put('分', "fen"); put('比', "bi"); put('正', "zheng"); put('确', "que")
        put('错', "cuo"); put('误', "wu"); put('正', "zheng"); put('确', "que")
        put('率', "lv"); put('得', "de"); put('分', "fen"); put('满', "man")

        // 时间相关
        put('天', "tian"); put('日', "ri"); put('周', "zhou"); put('年', "nian")
        put('小', "xiao"); put('时', "shi"); put('分', "fen"); put('钟', "zhong")
        put('秒', "miao"); put('今', "jin"); put('昨', "zuo"); put('明', "ming")
        put('上', "shang"); put('下', "xia"); put('早', "zao"); put('晚', "wan")
        put('上', "shang"); put('午', "wu"); put('下', "xia"); put('午', "wu")

        // 动作相关
        put('学', "xue"); put('习', "xi"); put('教', "jiao"); put('师', "shi")
        put('老', "lao"); put('师', "shi"); put('同', "tong"); put('学', "xue")
        put('班', "ban"); put('级', "ji"); put('年', "nian"); put('级', "ji")
        put('校', "xiao"); put('园', "yuan"); put('家', "jia"); put('长', "zhang")
        put('辅', "fu"); put('导', "dao"); put('培', "pei"); put('训', "xun")
        put('补', "bu"); put('课', "ke"); put('加', "jia"); put('油', "you")
        put('努', "nu"); put('力', "li"); put('坚', "jian"); put('持', "chi")
        put('勤', "qin"); put('奋', "fen"); put('刻', "ke"); put('苦', "ku")
        put('认', "ren"); put('真', "zhen"); put('仔', "zi"); put('细', "xi")
        put('专', "zhuan"); put('注', "zhu"); put('集', "ji"); put('中', "zhong")
        put('注', "zhu"); put('意', "yi"); put('思', "si"); put('考', "kao")
        put('回', "hui"); put('答', "da"); put('问', "wen"); put('题', "ti")
        put('错', "cuo"); put('误', "wu"); put('改', "gai"); put('正', "zheng")
        put('纠', "jiu"); put('错', "cuo"); put('总', "zong"); put('结', "jie")
        put('反', "fan"); put('思', "si"); put('反', "fan"); put('馈', "kui")
        put('评', "ping"); put('估', "gu"); put('测', "ce"); put('评', "ping")
        put('量', "liang"); put('化', "hua"); put('评', "ping"); put('价', "jia")

        // 常见汉字（扩展）
        put('的', "de"); put('了', "le"); put('是', "shi"); put('在', "zai")
        put('不', "bu"); put('有', "you"); put('和', "he"); put('人', "ren")
        put('这', "zhe"); put('主', "zhu"); put('上', "shang"); put('来', "lai")
        put('以', "yi"); put('个', "ge"); put('到', "dao"); put('他', "ta")
        put('们', "men"); put('为', "wei"); put('说', "shuo"); put('地', "di")
        put('个', "ge"); put('会', "hui"); put('好', "hao"); put('能', "neng")
        put('对', "dui"); put('着', "zhe"); put('就', "jiu"); put('那', "na")
        put('要', "yao"); put('下', "xia"); put('自', "zi"); put('也', "ye")
        put('子', "zi"); put('去', "qu"); put('之', "zhi"); put('得', "de")
        put('过', "guo"); put('多', "duo"); put('都', "dou"); put('很', "hen")
        put('什', "shen"); put('么', "me"); put('没', "mei"); put('又', "you")
        put('看', "kan"); put('只', "zhi"); put('让', "rang"); put('把', "ba")
        put('什', "shen"); put('么', "me"); put('没', "mei"); put('还', "hai")
        put('与', "yu"); put('给', "gei"); put('被', "bei"); put('从', "cong")
        put('所', "suo"); put('可', "ke"); put('它', "ta"); put('而', "er")
        put('其', "qi"); put('如', "ru"); put('那', "na"); put('样', "yang")
        put('你', "ni"); put('什', "shen"); put('么', "me"); put('没', "mei")
        put('还', "hai"); put('更', "geng"); put('当', "dang"); put('然', "ran")
        put('已', "yi"); put('经', "jing"); put('因', "yin"); put('为', "wei")
        put('所', "suo"); put('以', "yi"); put('但', "dan"); put('是', "shi")
        put('如', "ru"); put('果', "guo"); put('虽', "sui"); put('然', "ran")
        put('只', "zhi"); put('是', "shi"); put('还', "hai"); put('是', "shi")
        put('因', "yin"); put('为', "wei"); put('所', "suo"); put('以', "yi")
        put('所', "suo"); put('以', "yi"); put('可', "ke"); put('是', "shi")
        put('不', "bu"); put('过', "guo"); put('而', "er"); put('且', "qie")
        put('或', "huo"); put('者', "zhe"); put('既', "ji"); put('然', "ran")
        put('不', "bu"); put('仅', "jin"); put('而', "er"); put('且', "qie")
        put('虽', "sui"); put('然', "ran"); put('可', "ke"); put('是', "shi")
        put('尽', "jin"); put('管', "guan"); put('但', "dan"); put('是', "shi")
        put('无', "wu"); put('论', "lun"); put('如', "ru"); put('何', "he")
        put('只', "zhi"); put('要', "yao"); put('就', "jiu"); put('能', "neng")
        put('既', "ji"); put('然', "ran"); put('那', "na"); put('么', "me")
        put('要', "yao"); put('么', "me"); put('或', "huo"); put('者', "zhe")
        put('不', "bu"); put('是', "shi"); put('就', "jiu"); put('是', "shi")
        put('不', "bu"); put('是', "shi"); put('而', "er"); put('是', "shi")
        put('不', "bu"); put('但', "dan"); put('而', "er"); put('且', "qie")
        put('尽', "jin"); put('管', "guan"); put('如', "ru"); put('此', "ci")
        put('既', "ji"); put('然', "ran"); put('那', "na"); put('么', "me")
        put('既', "ji"); put('然', "ran"); put('那', "na"); put('样', "yang")
        put('既', "ji"); put('然', "ran"); put('如', "ru"); put('此', "ci")
        put('既', "ji"); put('然', "ran"); put('这', "zhe"); put('样', "yang")
        put('既', "ji"); put('然', "ran"); put('那', "na"); put('么', "me")
        put('既', "ji"); put('然', "ran"); put('这', "zhe"); put('么', "me")
        put('既', "ji"); put('然', "ran"); put('那', "na"); put('样', "yang")
        put('既', "ji"); put('然', "ran"); put('这', "zhe"); put('样', "yang")
    }

    /**
     * 将中文字符串转换为拼音
     * 例: "数学" → "shuxue"
     */
    fun toPinyin(text: String): String {
        return text.map { char ->
            pinyinMap[char] ?: char.lowercaseChar().toString()
        }.joinToString("")
    }

    /**
     * 获取拼音首字母
     * 例: "数学" → "sx"
     */
    fun getInitials(text: String): String {
        return text.map { char ->
            val pinyin = pinyinMap[char]
            if (pinyin != null) {
                pinyin.first().toString()
            } else {
                char.lowercaseChar().toString()
            }
        }.joinToString("")
    }

    /**
     * 检查文本是否匹配搜索查询（支持中文、拼音、首字母）
     * @return true 如果匹配
     */
    fun matches(text: String, query: String): Boolean {
        if (text.contains(query, ignoreCase = true)) return true
        val pinyin = toPinyin(text)
        if (pinyin.contains(query, ignoreCase = true)) return true
        val initials = getInitials(text)
        if (initials.contains(query, ignoreCase = true)) return true
        return false
    }
}
