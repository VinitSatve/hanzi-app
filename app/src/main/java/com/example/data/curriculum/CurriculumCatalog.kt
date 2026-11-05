package com.example.data.curriculum

object CurriculumCatalog {

    val pinyinItems: List<PinyinItem> = listOf(
        // Initials (21 standard initials represented with key high-priority groups)
        PinyinItem("ini_b", "b", "INITIAL", "[p]", "Unaspirated 'p' as in 'spin'", "爸", "爸", "bà", "father", "Avoid voicing like English 'b'; keep it crisp and unaspirated."),
        PinyinItem("ini_p", "p", "INITIAL", "[pʰ]", "Aspirated 'p' as in 'pin' with strong puff of air", "怕", "怕", "pà", "to fear", "Ensure a noticeable burst of breath."),
        PinyinItem("ini_m", "m", "INITIAL", "[m]", "Same as English 'm' in 'mother'", "妈", "媽", "mā", "mother", "Keep nasal resonance steady across the tone."),
        PinyinItem("ini_f", "f", "INITIAL", "[f]", "Same as English 'f' in 'far'", "发", "發", "fā", "to send / emit", "Light lower-lip contact with upper teeth."),
        PinyinItem("ini_d", "d", "INITIAL", "[t]", "Unaspirated 't' as in 'stop'", "大", "大", "dà", "big", "Tongue tip touches upper alveolar ridge without aspiration."),
        PinyinItem("ini_t", "t", "INITIAL", "[tʰ]", "Aspirated 't' as in 'top'", "他", "他", "tā", "he / him", "Strong puff of air on release."),
        PinyinItem("ini_n", "n", "INITIAL", "[n]", "Same as English 'n' in 'no'", "你", "你", "nǐ", "you", "Distinct from 'l' by nasal airflow."),
        PinyinItem("ini_l", "l", "INITIAL", "[l]", "Clear 'l' as in 'love'", "来", "來", "lái", "to come", "Flick tongue tip cleanly off the ridge."),
        PinyinItem("ini_g", "g", "INITIAL", "[k]", "Unaspirated 'k' as in 'sky'", "高", "高", "gāo", "tall / high", "Do not voice like English 'g'."),
        PinyinItem("ini_k", "k", "INITIAL", "[kʰ]", "Aspirated 'k' as in 'kite'", "看", "看", "kàn", "to look / read", "Strong velar aspiration."),
        PinyinItem("ini_h", "h", "INITIAL", "[x]", "Throaty 'h' like Scottish 'loch'", "好", "好", "hǎo", "good", "Slightly more friction at the soft palate than English 'h'."),
        PinyinItem("ini_j", "j", "INITIAL", "[tɕ]", "Alveolo-palatal 'j', tongue tip behind lower teeth", "家", "家", "jiā", "home / family", "Keep tongue tip DOWN behind lower teeth, never curled back."),
        PinyinItem("ini_q", "q", "INITIAL", "[tɕʰ]", "Aspirated palatal 'ch' with flat tongue", "去", "去", "qù", "to go", "Like 'ch' in 'cheese' with a strong puff and tongue tip down."),
        PinyinItem("ini_x", "x", "INITIAL", "[ɕ]", "Palatal 'sh' with spread lips", "谢", "謝", "xiè", "to thank", "Smile slightly while producing a crisp 'sh' with tongue tip down."),
        PinyinItem("ini_zh", "zh", "INITIAL", "[ʈʂ]", "Retroflex 'j' with tongue tip curled back", "中", "中", "zhōng", "middle / China", "Curl tongue tip toward hard palate (retroflex)."),
        PinyinItem("ini_ch", "ch", "INITIAL", "[ʈʂʰ]", "Retroflex aspirated 'ch'", "吃", "吃", "chī", "to eat", "Curl tongue tip back and release with a strong puff of air."),
        PinyinItem("ini_sh", "sh", "INITIAL", "[ʂ]", "Retroflex 'sh' as in 'shirt'", "是", "是", "shì", "to be", "Keep tongue curled back; contrast with palatal 'x'."),
        PinyinItem("ini_r", "r", "INITIAL", "[ʐ]", "Retroflex buzzed 'r', no lip rounding", "人", "人", "rén", "person", "Like French 'j' + English 'r' with tongue tip curled up."),
        PinyinItem("ini_z", "z", "INITIAL", "[ts]", "Unaspirated 'ds' as in 'kids'", "在", "在", "zài", "at / in", "Tongue tip touches back of upper teeth."),
        PinyinItem("ini_c", "c", "INITIAL", "[tsʰ]", "Aspirated 'ts' as in 'cats'", "菜", "菜", "cài", "dish / vegetable", "Release 'ts' with a sharp puff of air."),
        PinyinItem("ini_s", "s", "INITIAL", "[s]", "Crisp 's' as in 'sun'", "三", "三", "sān", "three", "Flat tongue tip behind teeth."),

        // Finals
        PinyinItem("fin_a", "a", "FINAL", "[ä]", "Open 'ah' as in 'father'", "妈", "媽", "mā", "mother", "Open mouth wide and keep vowel pure."),
        PinyinItem("fin_o", "o", "FINAL", "[uɔ]", "Rounded 'aw' with slight glide", "我", "我", "wǒ", "I / me", "Round lips smoothly."),
        PinyinItem("fin_e", "e", "FINAL", "[ɤ]", "Unrounded back 'uh' as in 'her'", "喝", "喝", "hē", "to drink", "Do not round the lips; pull tongue slightly back."),
        PinyinItem("fin_i", "i", "FINAL", "[i]", "Long 'ee' as in 'see' (or buzzed after z/c/s/zh/ch/sh/r)", "一", "一", "yī", "one", "After zh/ch/sh/r/z/c/s, hold tongue position and vocalize."),
        PinyinItem("fin_u", "u", "FINAL", "[u]", "Rounded 'oo' as in 'food'", "书", "書", "shū", "book", "Protrude lips slightly."),
        PinyinItem("fin_v", "ü", "FINAL", "[y]", "'ee' spoken with tightly rounded lips", "女", "女", "nǚ", "woman / female", "Say 'ee' then round lips without moving tongue. Written as 'u' after j/q/x."),
        PinyinItem("fin_ai", "ai", "FINAL", "[aɪ]", "Like 'eye'", "爱", "愛", "ài", "to love", "Start wide on 'a' and glide to 'i'."),
        PinyinItem("fin_ei", "ei", "FINAL", "[eɪ]", "Like 'ay' in 'day'", "北", "北", "běi", "north", "Keep glide crisp."),
        PinyinItem("fin_ao", "ao", "FINAL", "[ɑʊ]", "Like 'ow' in 'cow'", "好", "好", "hǎo", "good", "Round lips at the end of the diphthong."),
        PinyinItem("fin_ou", "ou", "FINAL", "[oʊ]", "Like 'oh' in 'go'", "走", "走", "zǒu", "to walk", "Avoid cutting off the final 'u' glide."),
        PinyinItem("fin_an", "an", "FINAL", "[an]", "Front nasal 'ahn'", "安", "安", "ān", "peace / safe", "End with tongue tip touching upper teeth ridge."),
        PinyinItem("fin_ang", "ang", "FINAL", "[ɑŋ]", "Back nasal 'ahng'", "忙", "忙", "máng", "busy", "Resonate deep in the soft palate."),
        PinyinItem("fin_en", "en", "FINAL", "[ən]", "Like 'taken'", "本", "本", "běn", "root / volume", "Front nasal ending."),
        PinyinItem("fin_eng", "eng", "FINAL", "[ɤŋ]", "Back nasal 'ung'", "冷", "冷", "lěng", "cold", "Contrast front -en vs back -eng."),
        PinyinItem("fin_ian", "ian", "FINAL", "[jɛn]", "Sounds like 'yen'", "天", "天", "tiān", "sky / day", "Note: 'a' in -ian sounds like 'eh' [ɛ], not 'ah'."),
        PinyinItem("fin_uang", "uang", "FINAL", "[wɑŋ]", "Glide u + ang", "光", "光", "guāng", "light", "Smooth transition from rounded 'u' to open 'ang'."),

        // Tone Pairs & Sandhi
        PinyinItem("tp_33", "3rd + 3rd → 2nd + 3rd", "TONE_PAIR", "214 + 214 → 35 + 214", "Tone Sandhi: First 3rd tone rises like 2nd tone", "你好", "你好", "nǐ hǎo (ní hǎo)", "Hello", "Never dip both 3rd tones fully; raise the first syllable to 2nd tone."),
        PinyinItem("tp_bu4", "不 before 4th → 2nd", "TONE_PAIR", "bù + 4th → bú + 4th", "Tone Sandhi on 不 (bù)", "不是", "不是", "bú shì", "is not", "Change bù (4th) to bú (2nd) before another 4th-tone syllable."),
        PinyinItem("tp_yi1", "一 tone change", "TONE_PAIR", "yī → yí (before 4th) / yì (before 1/2/3)", "Dynamic tone shift on 一 (one)", "一个 / 一天", "一個 / 一天", "yí gè / yì tiān", "one item / one day", "Standalone 'yī' is 1st tone, but shifts in connected speech."),
        PinyinItem("tp_14", "1st + 4th Contrast", "TONE_PAIR", "55 + 51", "High flat followed by sharp fall", "今天 / 高兴", "今天 / 高興", "jīntiān / gāoxìng", "today / happy", "Keep the 1st tone high and steady before dropping sharply on the 4th.")
    )

    val lessons: List<CurriculumLesson> = listOf(
        CurriculumLesson(
            id = "pinyin_initials",
            stage = 0,
            hskStandard = "BOTH",
            title = "Stage 0.1 · Four Tones & Initials",
            subtitle = "Pitch contours, aspirated vs. unaspirated stops, and palatal sounds",
            chineseFocus = "妈 麻 马 骂 · 你好",
            traditionalFocus = "媽 麻 馬 罵 · 你好",
            pinyinFocus = "mā má mǎ mà · nǐ hǎo",
            summary = "Mandarin is a tonal language where pitch contour determines meaning. Master the 4 main tones (high flat, rising, low dipping, sharp falling), the neutral tone, and the distinction between unaspirated (b, d, g, j, z, zh) and aspirated (p, t, k, q, c, ch) initials.",
            keyPoints = listOf(
                "1st Tone (55): High and level pitch — mā (妈, mother).",
                "2nd Tone (35): Rising pitch like asking 'What?' — má (麻, hemp).",
                "3rd Tone (214): Low dipping pitch — mǎ (马, horse).",
                "4th Tone (51): Sharp falling pitch from top to bottom — mà (骂, scold).",
                "3rd Tone Sandhi: When two 3rd tones meet (nǐ + hǎo), pronounce the first as 2nd tone (ní hǎo)."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_p0_1",
                    question = "How is '你好 (nǐ hǎo)' naturally pronounced according to 3rd-tone sandhi?",
                    promptChinese = "你好",
                    promptPinyin = "nǐ hǎo",
                    options = listOf("ní hǎo (2nd + 3rd tone)", "nī hāo (1st + 1st tone)", "nì hào (4th + 4th tone)", "nǐ hāo (3rd + 1st tone)"),
                    correctIndex = 0,
                    explanation = "When two 3rd-tone syllables occur consecutively, the first shifts to a 2nd (rising) tone: ní hǎo."
                ),
                LessonExercise(
                    id = "ex_p0_2",
                    question = "Which Pinyin initial requires keeping the tongue tip DOWN behind the lower teeth?",
                    promptChinese = "家 / 去 / 谢",
                    promptPinyin = "j / q / x",
                    options = listOf("j, q, x (Palatals)", "zh, ch, sh (Retroflexes)", "g, k, h (Velars)", "f, m, p (Labials)"),
                    correctIndex = 0,
                    explanation = "Palatal initials (j, q, x) are articulated with the flat blade of the tongue while the tip rests behind the lower teeth."
                )
            )
        ),
        CurriculumLesson(
            id = "pinyin_finals",
            stage = 0,
            hskStandard = "BOTH",
            title = "Stage 0.2 · Compound Finals & Spelling Rules",
            subtitle = "Mastering ü, -ian, -en vs -eng, and tone mark placement",
            chineseFocus = "女生 · 每天 · 便宜",
            traditionalFocus = "女生 · 每天 · 便宜",
            pinyinFocus = "nǚshēng · měitiān · piányi",
            summary = "Understand how compound finals blend vowels and nasal endings, why 'ü' loses its umlaut dots after j, q, x, and y, and how Pinyin tone marks follow the 'a > o/e > i/u' priority rule.",
            keyPoints = listOf(
                "After j, q, x, and y, the vowel 'ü' is written as 'u' (e.g., 去 qù, 鱼 yú) because j/q/x never combine with regular 'u'.",
                "In '-ian' and '-üan', the 'a' sounds like 'eh' [ɛ] (e.g., 天 tiān rhymes with English 'yen').",
                "Tone placement priority: Place tone mark on 'a' first, then 'o' or 'e'. For 'iu' and 'ui', the tone goes on the SECOND vowel (liú, guì)."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_p0_3",
                    question = "In the syllable 'qù (去, to go)', what vowel sound does 'u' actually represent?",
                    promptChinese = "去",
                    promptPinyin = "qù",
                    options = listOf("The rounded front vowel ü [y]", "The back vowel u [u]", "The schwa e [ə]", "The diphthong ou [oʊ]"),
                    correctIndex = 0,
                    explanation = "After j, q, x, and y, umlaut dots on ü are omitted in standard Pinyin orthography."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk1_greetings",
            stage = 1,
            hskStandard = "BOTH",
            title = "Stage 1.1 · HSK 1 Greetings & Introductions",
            subtitle = "Personal pronouns, 是 (shì), 叫 (jiào), and polite questions",
            chineseFocus = "你好，我叫林美。你是哪国人？",
            traditionalFocus = "你好，我叫林美。你是哪國人？",
            pinyinFocus = "Nǐ hǎo, wǒ jiào Lín Měi. Nǐ shì nǎ guó rén?",
            summary = "Build essential self-introductions using 我 (wǒ), 你 (nǐ), 您 (nín, polite you), the verb 叫 (jiào, to be called), and the copula 是 (shì, to be).",
            keyPoints = listOf(
                "Subject + 叫 + Name: 我叫大卫 (Wǒ jiào Dàwèi — My name is David).",
                "Subject + 是 + Noun: 我是学生 (Wǒ shì xuéshēng — I am a student).",
                "Question particle 吗 (ma): Add 吗 to the end of any statement to turn it into a yes/no question.",
                "Polite form: Use 您好 (Nín hǎo) when addressing elders, teachers, or professionals."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h1_1",
                    question = "Choose the correct Mandarin sentence for 'Are you a teacher?'",
                    promptChinese = "你是老师吗？",
                    promptPinyin = "Nǐ shì lǎoshī ma?",
                    options = listOf("你是老师吗？", "你很老师吗？", "你有老师呢？", "你叫老师吧？"),
                    correctIndex = 0,
                    explanation = "Use 是 (shì) to link two nouns (你 and 老师) and add 吗 (ma) at the end for a yes/no question."
                ),
                LessonExercise(
                    id = "ex_h1_2",
                    question = "How do you negate 是 (shì)?",
                    promptChinese = "我不是中国人。",
                    promptPinyin = "Wǒ bú shì Zhōngguórén.",
                    options = listOf("不是 (bú shì)", "没是 (méi shì)", "别是 (bié shì)", "无是 (wú shì)"),
                    correctIndex = 0,
                    explanation = "是 (shì) is always negated with 不 (bù), never 没 (méi)."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk1_numbers_time",
            stage = 1,
            hskStandard = "BOTH",
            title = "Stage 1.2 · HSK 1 Numbers, Dates & Daily Time",
            subtitle = "Counting 1–100, telling time with 点 (diǎn) and 分 (fēn), and dates",
            chineseFocus = "现在早上八点半，我今天去学校。",
            traditionalFocus = "現在早上八點半，我今天去學校。",
            pinyinFocus = "Xiànzài zǎoshang bā diǎn bàn, wǒ jīntiān qù xuéxiào.",
            summary = "Mandarin follows a logical big-to-small order for time: Year (年) → Month (月) → Day (日/号) → Period of day → Hour (点) → Minute (分).",
            keyPoints = listOf(
                "Big-to-Small Principle: 今天早上八点 (Today morning 8 o'clock) — time expressions always go BEFORE the verb or at the start of the sentence.",
                "二 (èr) vs 两 (liǎng): Use 二 for counting/dates (二月 February), but 两 before measure words and hours (两点 2 o'clock, 两个人 two people)."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h1_3",
                    question = "How do you say '2:00' (two o'clock) in Mandarin?",
                    promptChinese = "两点",
                    promptPinyin = "liǎng diǎn",
                    options = listOf("两点 (liǎng diǎn)", "二点 (èr diǎn)", "双点 (shuāng diǎn)", "再点 (zài diǎn)"),
                    correctIndex = 0,
                    explanation = "When telling time on the hour for 2 o'clock, Mandarin uses 两点 (liǎng diǎn), not 二点."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk2_daily_comparisons",
            stage = 2,
            hskStandard = "BOTH",
            title = "Stage 2.1 · HSK 2 Daily Routines, Weather & 比 (bǐ)",
            subtitle = "Making comparisons, modal verbs (要, 可以, 能), and aspect particle 过",
            chineseFocus = "今天比昨天冷得多，你要多穿一件衣服。",
            traditionalFocus = "今天比昨天冷得多，你要多穿一件衣服。",
            pinyinFocus = "Jīntiān bǐ zuótiān lěng de duō, nǐ yào duō chuān yí jiàn yīfu.",
            summary = "Express comparisons cleanly using A 比 B + Adjective, talk about past life experiences with Verb + 过 (guò), and navigate shopping, weather, and daily transport.",
            keyPoints = listOf(
                "A 比 B + Adjective: 今天比昨天热 (Today is hotter than yesterday). Never put 很 (hěn) before the adjective in a 比 sentence!",
                "Adding degree after 比: Use 一点儿 (a little) or 得多 / 多了 (much more) AFTER the adjective: 快得多 (much faster).",
                "Experiential 过 (guò): 我去过北京 (Wǒ qùguo Běijīng — I have been to Beijing before)."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h2_1",
                    question = "Which sentence correctly says 'Airplanes are faster than trains'?",
                    promptChinese = "飞机比火车快。",
                    promptPinyin = "Fēijī bǐ huǒchē kuài.",
                    options = listOf("飞机比火车快。", "飞机比火车很快。", "飞机快比火车。", "比飞机火车快。"),
                    correctIndex = 0,
                    explanation = "In a 比 (bǐ) comparison, the structure is A 比 B + Adjective, and 很 (hěn) must not be used before the adjective."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk3_complements_ba",
            stage = 3,
            hskStandard = "BOTH",
            title = "Stage 3.1 · HSK 3 Result Complements & 把 (bǎ) Construction",
            subtitle = "Handling objects with 把, directional complements, and complex connectors",
            chineseFocus = "请把桌上的那本书拿过来，我已经看完了。",
            traditionalFocus = "請把桌上的那本書拿過來，我已經看完了。",
            pinyinFocus = "Qǐng bǎ zhuō shàng de nà běn shū ná guòlái, wǒ yǐjīng kàn wán le.",
            summary = "Transition from beginner sentences to natural Chinese syntax by mastering the 把 (bǎ) disposal structure and result/directional complements (看完, 听懂, 拿过来).",
            keyPoints = listOf(
                "Subject + 把 + Object + Verb + Complement/Element: Places a known, specific object before the verb to emphasize how it is affected or relocated.",
                "Result complements attach directly to the verb: 看完 (finish reading), 听懂 (understand by listening), 找到 (succeed in finding)."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h3_1",
                    question = "Choose the grammatically correct 把 (bǎ) sentence for 'He drank up the coffee':",
                    promptChinese = "他把咖啡喝完了。",
                    promptPinyin = "Tā bǎ kāfēi hē wán le.",
                    options = listOf("他把咖啡喝完了。", "他把喝咖啡了。", "他喝把咖啡完了。", "把咖啡他喝。"),
                    correctIndex = 0,
                    explanation = "A 把 sentence requires a result/complement or post-verbal element (such as 完 le) after the main verb."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk4_passive_discourse",
            stage = 4,
            hskStandard = "BOTH",
            title = "Stage 4.1 · HSK 4 Passive 被 (bèi) & Discourse Connectors",
            subtitle = "Expressing passive voice, 不仅...而且..., 尽管...但是..., and workplace fluency",
            chineseFocus = "无论遇到什么困难，我们都应该保持冷静并寻找解决办法。",
            traditionalFocus = "無論遇到什麼困難，我們都應該保持冷靜並尋找解決辦法。",
            pinyinFocus = "Wúlùn yùdào shénme kùnnan, wǒmen dōu yīnggāi bǎochí lěngjìng bìng xúnzhǎo jiějué bànfǎ.",
            summary = "Build multi-clause arguments, read news briefs, and express passive events with 被 (bèi), 让 (ràng), and 叫 (jiào).",
            keyPoints = listOf(
                "Receiver + 被 + (Agent) + Verb + Result: 我的手机被弟弟拿走了 (My phone was taken away by my younger brother).",
                "无论 (wúlùn, no matter what) pairs with 都 (dōu) in the second clause."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h4_1",
                    question = "Which particle completes the pair '无论天气怎么样，我___去跑步'?",
                    promptChinese = "无论...都...",
                    promptPinyin = "wúlùn... dōu...",
                    options = listOf("都 (dōu)", "才 (cái)", "刚 (gāng)", "只 (zhǐ)"),
                    correctIndex = 0,
                    explanation = "无论 (no matter) is paired with 都 (all/still) in the main clause."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk5_formal_register",
            stage = 5,
            hskStandard = "BOTH",
            title = "Stage 5.1 · HSK 5 Formal Written Chinese (书面语) & Idioms",
            subtitle = "Academic transitions, 既然...就..., 与其...不如..., and four-character chengyu",
            chineseFocus = "与其在原地犹豫不决，不如立刻采取行动。",
            traditionalFocus = "與其在原地猶豫不決，不如立刻採取行動。",
            pinyinFocus = "Yǔqí zài yuándì yóuyù bù jué, bùrú lìkè cǎiqǔ xíngdòng.",
            summary = "Bridge spoken Mandarin and formal written register (书面语) used in essays, business proposals, and journalism.",
            keyPoints = listOf(
                "与其 A，不如 B (Rather than A, it is better to B): Evaluates two choices and strongly recommends B.",
                "犹豫不决 (yóuyù bù jué): Idiom meaning 'hesitant and undecided'."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h5_1",
                    question = "What does the pattern '与其 A，不如 B' express?",
                    promptChinese = "与其...不如...",
                    promptPinyin = "yǔqí... bùrú...",
                    options = listOf("Rather than A, it is better to choose B", "Because of A, B happened", "Not only A, but also B", "Even though A, still B"),
                    correctIndex = 0,
                    explanation = "与其 A，不如 B weighs two options and states that B is preferable to A."
                )
            )
        ),
        CurriculumLesson(
            id = "hsk6_literary_mastery",
            stage = 6,
            hskStandard = "BOTH",
            title = "Stage 6.1 · HSK 6 Classical Nuance, Rhetoric & Idiomatic Precision",
            subtitle = "Advanced near-synonym discrimination, classical structures, and debate",
            chineseFocus = "唯有持之以恒地钻研，方能融会贯通、游刃有余。",
            traditionalFocus = "唯有持之以恆地鑽研，方能融會貫通、游刃有餘。",
            pinyinFocus = "Wéiyǒu chízhīyǐhéng de zuānyán, fāng néng rónghuì-guàntōng, yóurèn-yǒuyú.",
            summary = "Master C1/C2 Mandarin rhetoric, classical connectors (唯有...方能...), and high-frequency HSK 6 chengyu for professional and literary mastery.",
            keyPoints = listOf(
                "唯有 A，方能 B: Formal/literary equivalent of 只有 A，才能 B (Only by A can one B).",
                "持之以恒 (chízhīyǐhéng): To persevere consistently over time.",
                "融会贯通 (rónghuì-guàntōng): To achieve thorough comprehension by synthesizing knowledge."
            ),
            exercises = listOf(
                LessonExercise(
                    id = "ex_h6_1",
                    question = "What is the modern spoken equivalent of '唯有...方能...'?",
                    promptChinese = "唯有努力，方能成功。",
                    promptPinyin = "Wéiyǒu nǔlì, fāng néng chénggōng.",
                    options = listOf("只有...才能... (zhǐyǒu... cáinéng...)", "只要...就... (zhǐyào... jiù...)", "既然...那么... (jìrán... nàme...)", "宁可...也不... (nìngkě... yě bù...)"),
                    correctIndex = 0,
                    explanation = "唯有 (only) + 方能 (can then) is the elevated written counterpart to 只有...才能..."
                )
            )
        )
    )

    val vocabDatabase: List<VocabReference> = listOf(
        VocabReference("v_1", "你好", "你好", "nǐ hǎo", "Hello; how do you do", "Greeting", 1, "HSK_2", "Greetings", "你好，请问你贵姓？", "你好，請問你貴姓？", "Nǐ hǎo, qǐngwèn nǐ guìxìng?", "Hello, may I ask your family name?", listOf("您好 (nín hǎo)", "大家好 (dàjiā hǎo)"), "Universal greeting; first syllable rises to 2nd tone due to tone sandhi."),
        VocabReference("v_2", "谢谢", "謝謝", "xièxie", "To thank; thank you", "Verb", 1, "HSK_2", "Greetings", "非常感谢你的帮助，谢谢！", "非常感謝你的幫助，謝謝！", "Fēicháng gǎnxiè nǐ de bāngzhù, xièxie!", "Thank you so much for your help!", listOf("感谢 (gǎnxiè)", "不客气 (bú kèqi)"), "Second syllable is pronounced in neutral tone."),
        VocabReference("v_3", "学习", "學習", "xuéxí", "To study; to learn", "Verb", 1, "HSK_2", "Education", "我每天晚上学习汉字。", "我每天晚上學習漢字。", "Wǒ měitiān wǎnshang xuéxí Hànzì.", "I study Chinese characters every evening.", listOf("学生 (xuéshēng)", "学校 (xuéxiào)"), "Used for both general study and skill acquisition."),
        VocabReference("v_4", "朋友", "朋友", "péngyou", "Friend", "Noun", 1, "HSK_2", "People", "他是我的好朋友。", "他是我的好朋友。", "Tā shì wǒ de hǎo péngyou.", "He is my good friend.", listOf("友谊 (yǒuyì)", "交朋友 (jiāo péngyou)"), "Measure word is 个 (gè) or 位 (wèi, polite)."),
        VocabReference("v_5", "高", "高", "gāo", "Tall; high", "Adjective", 1, "HSK_3", "Descriptions", "这座山非常高。", "這座山非常高。", "Zhè zuò shān fēicháng gāo.", "This mountain is very high.", listOf("高兴 (gāoxìng)", "提高 (tígāo)"), "In HSK 3.0 Band 1, 高 is taught both as a standalone adjective and in compounds."),
        VocabReference("v_6", "准备", "準備", "zhǔnbèi", "To prepare; to intend", "Verb / Noun", 2, "HSK_2", "Daily Life", "我已经准备好明天的考试了。", "我已經準備好明天的考試了。", "Wǒ yǐjīng zhǔnbèi hǎo míngtiān de kǎoshì le.", "I have already prepared for tomorrow's exam.", listOf("打算 (dǎsuàn)", "预备 (yùbèi)"), "Often followed by 好 (hǎo) as a result complement indicating readiness."),
        VocabReference("v_7", "觉得", "覺得", "juéde", "To feel; to think (opinion)", "Verb", 2, "HSK_2", "Cognition", "我觉得这道菜很好吃。", "我覺得這道菜很好吃。", "Wǒ juéde zhè dào cài hěn hǎochī.", "I think this dish is delicious.", listOf("认为 (rènwéi)", "感觉 (gǎnjué)"), "Expresses personal impression or subjective feeling."),
        VocabReference("v_8", "环境", "環境", "huánjìng", "Environment; surroundings", "Noun", 3, "HSK_2", "Society", "保护自然环境是每个人的责任。", "保護自然環境是每個人的責任。", "Bǎohù zìrán huánjìng shì měi gè rén de zérèn.", "Protecting the natural environment is everyone's responsibility.", listOf("自然 (zìrán)", "保护 (bǎohù)"), "Used for both ecological environment and work/study atmosphere."),
        VocabReference("v_9", "经验", "經驗", "jīngyàn", "Experience (skill/knowledge)", "Noun", 4, "HSK_2", "Workplace", "他有丰富的教学经验。", "他有豐富的教學經驗。", "Tā yǒu fēngfù de jiàoxué jīngyàn.", "He has rich teaching experience.", listOf("经历 (jīnglì)", "体验 (tǐyàn)"), "Contrast with 经历 (jīnglì): 经验 refers to accumulated know-how, while 经历 refers to events lived through."),
        VocabReference("v_10", "人工智能", "人工智能", "réngōng zhìnéng", "Artificial Intelligence (AI)", "Noun", 3, "HSK_3", "Technology", "人工智能正在改变现代教育。", "人工智能正在改變現代教育。", "Réngōng zhìnéng zhèngzài gǎibiàn xiàndài jiàoyù.", "Artificial intelligence is transforming modern education.", listOf("科技 (kējì)", "算法 (suànfǎ)"), "Included in the updated HSK 3.0 contemporary vocabulary list."),
        VocabReference("v_11", "效率", "效率", "xiàolǜ", "Efficiency", "Noun", 5, "HSK_2", "Workplace", "使用科学的方法可以提高学习效率。", "使用科學的方法可以提高學習效率。", "Shǐyòng kēxué de fāngfǎ kěyǐ tígāo xuéxí xiàolǜ.", "Using scientific methods can improve study efficiency.", listOf("效果 (xiàoguǒ)", "高效 (gāoxiào)"), "Pairs with 提高 (tígāo, to raise/improve)."),
        VocabReference("v_12", "持之以恒", "持之以恆", "chí zhī yǐ héng", "To persevere unremittingly", "Idiom (Chengyu)", 6, "HSK_2", "Literature", "学习语言需要持之以恒的精神。", "學習語言需要持之以恆的精神。", "Xuéxí yǔyán xūyào chízhīyǐhéng de jīngshén.", "Learning a language requires a spirit of perseverance.", listOf("坚持不懈 (jiānchí búxiè)", "半途而废 (bàntú ér fèi)"), "Four-character idiom emphasizing steady, long-term effort.")
    )
}
