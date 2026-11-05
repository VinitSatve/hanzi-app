package com.example.data.curriculum

object CurriculumExtras {

    val characters: List<CharacterStrokeData> = listOf(
        CharacterStrokeData(
            id = "char_ren",
            simplified = "人",
            traditional = "人",
            pinyin = "rén",
            meaning = "Person; human being",
            hskLevel = 1,
            radical = "人 (rén)",
            radicalMeaning = "Person (pictograph of a standing figure)",
            components = "Single pictographic component (2 strokes)",
            strokeCount = 2,
            strokeOrderNames = listOf("1. Pie (丿) — Left-falling sweep from top center", "2. Na (㇏) — Right-falling press starting mid-way down Pie"),
            similarCharacters = "入 (rù, to enter — right stroke starts higher) · 八 (bā, eight — strokes do not touch)",
            strokePaths = listOf(
                listOf(0.52f to 0.16f, 0.42f to 0.48f, 0.18f to 0.84f),
                listOf(0.45f to 0.38f, 0.62f to 0.65f, 0.84f to 0.84f)
            )
        ),
        CharacterStrokeData(
            id = "char_da",
            simplified = "大",
            traditional = "大",
            pinyin = "dà",
            meaning = "Big; large; great",
            hskLevel = 1,
            radical = "大 (dà)",
            radicalMeaning = "Person with arms stretched wide",
            components = "一 (one/horizon) + 人 (person)",
            strokeCount = 3,
            strokeOrderNames = listOf(
                "1. Heng (一) — Horizontal stroke left to right",
                "2. Pie (丿) — Left-falling curve through center",
                "3. Na (㇏) — Right-falling press from center intersection"
            ),
            similarCharacters = "太 (tài, too/extremely — adds bottom dot) · 天 (tiān, sky — adds top horizontal)",
            strokePaths = listOf(
                listOf(0.20f to 0.40f, 0.80f to 0.40f),
                listOf(0.50f to 0.14f, 0.44f to 0.54f, 0.18f to 0.86f),
                listOf(0.50f to 0.42f, 0.66f to 0.68f, 0.84f to 0.86f)
            )
        ),
        CharacterStrokeData(
            id = "char_zhong",
            simplified = "中",
            traditional = "中",
            pinyin = "zhōng",
            meaning = "Middle; center; China",
            hskLevel = 1,
            radical = "丨 (gǔn)",
            radicalMeaning = "Vertical line piercing the center",
            components = "口 (mouth/enclosure) + 丨 (vertical axis)",
            strokeCount = 4,
            strokeOrderNames = listOf(
                "1. Shu (丨) — Left vertical of box",
                "2. Hengzhe (𠃍) — Top horizontal and right turn",
                "3. Heng (一) — Bottom closing horizontal",
                "4. Shu (丨) — Central vertical piercing top to bottom"
            ),
            similarCharacters = "申 (shēn, to state) · 冲 (chōng, to rush)",
            strokePaths = listOf(
                listOf(0.24f to 0.34f, 0.24f to 0.62f),
                listOf(0.24f to 0.34f, 0.76f to 0.34f, 0.76f to 0.62f),
                listOf(0.24f to 0.62f, 0.76f to 0.62f),
                listOf(0.50f to 0.12f, 0.50f to 0.88f)
            )
        ),
        CharacterStrokeData(
            id = "char_wen",
            simplified = "文",
            traditional = "文",
            pinyin = "wén",
            meaning = "Language; script; culture; literature",
            hskLevel = 2,
            radical = "文 (wén)",
            radicalMeaning = "Crossed markings / literary pattern",
            components = "亠 (lid) + 乂 (crossed strokes)",
            strokeCount = 4,
            strokeOrderNames = listOf(
                "1. Dian (丶) — Top center dot",
                "2. Heng (一) — Wide horizontal stroke",
                "3. Pie (丿) — Left-falling curve from upper right",
                "4. Na (㇏) — Right-falling press from upper left"
            ),
            similarCharacters = "丈 (zhàng, ten feet) · 久 (jiǔ, long time)",
            strokePaths = listOf(
                listOf(0.48f to 0.14f, 0.53f to 0.25f),
                listOf(0.18f to 0.32f, 0.82f to 0.32f),
                listOf(0.66f to 0.32f, 0.48f to 0.64f, 0.20f to 0.86f),
                listOf(0.32f to 0.42f, 0.56f to 0.68f, 0.82f to 0.86f)
            )
        ),
        CharacterStrokeData(
            id = "char_ni",
            simplified = "你",
            traditional = "你",
            pinyin = "nǐ",
            meaning = "You (singular)",
            hskLevel = 1,
            radical = "亻 (dānrénpáng)",
            radicalMeaning = "Standing person radical (left side)",
            components = "亻 (person) + 尔 (phonetic ěr)",
            strokeCount = 7,
            strokeOrderNames = listOf(
                "1. Pie (丿) — Left person slant",
                "2. Shu (丨) — Left person vertical",
                "3. Pie (丿) — Top right hook slant",
                "4. Henggou (乛) — Horizontal with hook",
                "5. Shugou (亅) — Center vertical hook",
                "6. Dian (丶) — Left side dot",
                "7. Dian (丶) — Right side dot"
            ),
            similarCharacters = "您 (nín, polite you — adds 心 heart underneath) · 他 (tā, he)",
            strokePaths = listOf(
                listOf(0.34f to 0.16f, 0.16f to 0.50f),
                listOf(0.26f to 0.36f, 0.26f to 0.86f),
                listOf(0.56f to 0.16f, 0.42f to 0.40f),
                listOf(0.46f to 0.32f, 0.82f to 0.32f, 0.76f to 0.44f),
                listOf(0.62f to 0.36f, 0.62f to 0.84f, 0.54f to 0.78f),
                listOf(0.46f to 0.54f, 0.38f to 0.72f),
                listOf(0.74f to 0.54f, 0.84f to 0.72f)
            )
        ),
        CharacterStrokeData(
            id = "char_hao",
            simplified = "好",
            traditional = "好",
            pinyin = "hǎo",
            meaning = "Good; well; fine",
            hskLevel = 1,
            radical = "女 (nǚ)",
            radicalMeaning = "Woman / female (left radical)",
            components = "女 (woman) + 子 (child) — ideograph of goodness and harmony",
            strokeCount = 6,
            strokeOrderNames = listOf(
                "1. Pie-dian (ㄑ) — Left angle stroke of 女",
                "2. Pie (丿) — Right sweep of 女",
                "3. Ti (㇀) — Rising stroke across 女",
                "4. Heng-pie (㇇) — Top hook of 子",
                "5. Wan-gou (㇁) — Curved vertical hook of 子",
                "6. Heng (一) — Horizontal cross of 子"
            ),
            similarCharacters = "如 (rú, as if) · 她 (tā, she)",
            strokePaths = listOf(
                listOf(0.30f to 0.18f, 0.20f to 0.56f, 0.40f to 0.74f),
                listOf(0.40f to 0.32f, 0.22f to 0.82f),
                listOf(0.14f to 0.48f, 0.44f to 0.42f),
                listOf(0.54f to 0.24f, 0.82f to 0.24f, 0.66f to 0.42f),
                listOf(0.66f to 0.42f, 0.68f to 0.84f, 0.58f to 0.78f),
                listOf(0.50f to 0.52f, 0.88f to 0.52f)
            )
        )
    )

    // All 17 required grammar patterns from user specification
    val grammarTopics: List<GrammarTopic> = listOf(
        GrammarTopic("g_shi", "是 (shì)", "Equative Verb 'To Be'", "Noun A + 是 + Noun B", 1, "Connects two nouns or pronouns to state identity or classification. Never use 是 directly before an adjective in neutral statements.", "我是大学生。", "我是大學生。", "Wǒ shì dàxuéshēng.", "I am a university student.", listOf("Links Noun A to Noun B", "Negate with 不 (不是 bú shì), never 没"), "Saying *我是高 instead of 我很高.", "Use 很 + Adjective for descriptive predicates, and 是 + Noun for identity.", "Complete: '他 ___ 我的中文老师。'", listOf("是 (shì)", "很 (hěn)", "有 (yǒu)", "在 (zài)"), 0),
        GrammarTopic("g_you", "有 (yǒu)", "Possession & Existence 'To Have / There Is'", "Subject / Location + 有 + Object", 1, "Expresses ownership ('have') or existence at a location ('there is/are'). Always negated with 没 (没有 méiyǒu).", "我有两个中国朋友。", "我有兩個中國朋友。", "Wǒ yǒu liǎng gè Zhōngguó péngyou.", "I have two Chinese friends.", listOf("Negate ONLY with 没 (没有), never *不有", "Also used for past negation: 没(有) + Verb"), "Saying *我不有钱 instead of 我没有钱.", "是 identifies what something is; 有 states what someone possesses or what exists somewhere.", "How do you negate 有 (yǒu)?", listOf("没有 (méiyǒu)", "不有 (bù yǒu)", "别有 (bié yǒu)", "未有 (wèi yǒu)"), 0),
        GrammarTopic("g_zai", "在 (zài)", "Location Verb & Progressive Marker", "Subject + 在 + Place (+ Verb)", 1, "Functions as a verb meaning 'to be located at', a preposition 'at/in' before a verb, or an ongoing action marker ('currently doing').", "我在图书馆看书。", "我在圖書館看書。", "Wǒ zài túshūguǎn kàn shū.", "I am reading books in the library.", listOf("Location phrase (在 + Place) comes BEFORE the main verb in Mandarin", "在 + Verb indicates an action in progress"), "Putting the location at the end (*我看书在图书馆).", "English puts location after the verb, whereas Mandarin puts 在 + Location before the action verb.", "Choose the natural Mandarin word order for 'She works in Beijing':", listOf("她在北京工作。", "她工作在北京。", "在北京工作她。", "她北京在工作。"), 0),
        GrammarTopic("g_de", "的 (de)", "Possessive & Attributive Particle", "Modifier + 的 + Noun", 1, "Links a possessor, adjective phrase, or relative clause to the noun it modifies (always placed before the head noun).", "这是我妈妈做的菜。", "這是我媽媽做的菜。", "Zhè shì wǒ māma zuò de cài.", "This is the dish my mother cooked.", listOf("Relative clauses in Chinese come BEFORE 的 + Noun", "Omit 的 for very close personal relationships (我妈妈)"), "Putting the relative clause after the noun like English.", "Contrast 的 (noun modifier), 得 (post-verb complement), and 地 (adverbial modifier before verb).", "Translate 'the book I bought yesterday':", listOf("我昨天买的书", "书我昨天买的", "买的我昨天书", "昨天书我买的"), 0),
        GrammarTopic("g_le", "了 (le)", "Aspect Completion & Change of State", "Verb + 了 / Sentence + 了", 1, "Verb-final 了 marks completion of an action; sentence-final 了 signals a new situation or change of state.", "我已经吃了早饭。", "我已經吃了早飯。", "Wǒ yǐjīng chī le zǎofàn.", "I have already eaten breakfast.", listOf("了 marks completion (aspect), NOT past tense", "Do not use 了 with habitual actions or when negated with 没"), "Writing *我没去了昨天 (never combine 没 and 了 for completed action).", "过 (guò) emphasizes past life experience, while 了 (le) marks concrete completion or state change.", "Which sentence is grammatically INCORRECT?", listOf("我昨天没去了学校。", "我昨天没去学校。", "我昨天去了学校。", "下雨了。"), 0),
        GrammarTopic("g_ma", "吗 (ma)", "Yes/No Question Particle", "Statement + 吗？", 1, "Placed at the end of any declarative statement to turn it into a yes/no question without changing word order.", "你会说汉语吗？", "你會說漢語嗎？", "Nǐ huì shuō Hànyǔ ma?", "Can you speak Mandarin?", listOf("Do not combine 吗 with question words like 什么 or 哪儿", "Do not combine 吗 with A-not-A questions (是不是)"), "Saying *你是谁吗？ (WHO is already a question word).", "Use 吗 for pure yes/no questions; omit 吗 when using wh-question words.", "Which sentence uses 吗 correctly?", listOf("你喜欢喝茶吗？", "你叫什么名字吗？", "他是谁吗？", "你去不去北京吗？"), 0),
        GrammarTopic("g_ne", "呢 (ne)", "Reciprocal Question & Ongoing Particle", "Topic + 呢？ / 正在...呢", 1, "Asks 'What about X?' in follow-up questions, asks 'Where is X?', or softens ongoing statements.", "我很好，你呢？", "我很好，你呢？", "Wǒ hěn hǎo, nǐ ne?", "I am doing well, and you?", listOf("Noun + 呢？ without context means 'Where is [Noun]?' (我的手机呢？)", "In dialogue, 'A, B 呢?' means 'And what about B?'"), "Using 呢 to ask a brand-new yes/no question instead of 吗.", "吗 asks yes/no; 呢 bounces a question back or asks about whereabouts.", "If someone asks '我的钥匙呢？(Wǒ de yàoshi ne?)' with no prior context, what do they mean?", listOf("Where are my keys?", "Are these my keys?", "Do you have keys?", "Whose keys are these?"), 0),
        GrammarTopic("g_bu", "不 (bù)", "Present, Future & Habitual Negation", "不 + Verb / Adjective", 1, "Negates present/future intentions, habitual actions, adjectives, and cognitive/copula verbs like 是, 知道, 觉得.", "我不喝咖啡，我常喝茶。", "我不喝咖啡，我常喝茶。", "Wǒ bù hē kāfēi, wǒ cháng hē chá.", "I don't drink coffee; I often drink tea.", listOf("Tone Sandhi: 不 (bù) changes to 2nd tone (bú) before a 4th-tone syllable", "Used for subjective refusal or habitual negation"), "Using 不 to negate 有 (*不有).", "不 negates habits, will, and adjectives; 没 negates past completed actions and 有.", "How is 不 pronounced in 不要 (bù yào)?", listOf("bú yào (2nd + 4th)", "bù yào (4th + 4th)", "bū yào (1st + 4th)", "bǔ yào (3rd + 4th)"), 0),
        GrammarTopic("g_mei", "没 (méi)", "Past Objective Negation & Negating 有", "没(有) + Verb", 1, "Negates possession (没有) and states objectively that an action did not happen in the past.", "我昨天没看那部电影。", "我昨天沒看那部電影。", "Wǒ zuótiān méi kàn nà bù diànyǐng.", "I didn't watch that movie yesterday.", listOf("Never add 了 after a verb negated by 没", "Does not negate adjectives or 是"), "Saying *我没是学生 instead of 我不是学生.", "没 states an action simply hasn't occurred yet; 不 states refusal or general habit.", "Choose the correct negation for 'I didn't go yesterday':", listOf("我昨天没去。", "我昨天不去。", "我昨天没去了。", "我昨天不去了。"), 0),
        GrammarTopic("g_hen", "很 (hěn)", "Adjectival Predicate Linker", "Subject + 很 + Adjective", 1, "Links a subject to an adjective in simple declarative sentences. Often unstressed and neutral rather than strongly meaning 'very'.", "中文很有意思。", "中文很有意思。", "Zhōngwén hěn yǒuyìsi.", "Chinese is interesting.", listOf("Without 很, 'Subject + Adjective' implies a comparison (e.g. 他高 = He is taller)", "Omit 很 in negative (不高) and 比 comparison sentences"), "Using 是 before an adjective (*中文是有意思).", "很 serves as a grammatical buffer before adjectives in positive statements.", "How do you naturally say 'She is busy' in Mandarin?", listOf("她很忙。", "她是忙。", "她有忙。", "她在忙了。"), 0),
        GrammarTopic("g_ye", "也 (yě)", "Adverb 'Also / Too'", "Subject + 也 + Verb / Adjective", 1, "Indicates 'also' or 'too'. Must ALWAYS be placed after the subject and BEFORE the verb or negation.", "我也喜欢学习汉语。", "我也喜歡學習漢語。", "Wǒ yě xǐhuan xuéxí Hànyǔ.", "I also like studying Chinese.", listOf("Always precedes the verb or adjective (我也去, 我也不去)", "Never placed at the very end of a sentence like English 'too'"), "Placing 也 at the end of the sentence (*我喜欢汉语也).", "Both 也 (also) and 都 (all) are pre-verbal adverbs that must come after the subject and before the predicate.", "Where does 也 belong in 'He also doesn't know'?", listOf("他也不知道。", "他不也知道。", "他不知道也。", "也他不知道。"), 0),
        GrammarTopic("g_dou", "都 (dōu)", "Inclusive Adverb 'All / Both'", "Plural Subject + 都 + Verb / Adjective", 1, "Summarizes items that appear BEFORE it ('both' or 'all'). Always placed after the topic/subject and before the verb.", "我们都是中国人。", "我們都是中國人。", "Wǒmen dōu shì Zhōngguórén.", "We are all Chinese.", listOf("都 only refers backward to nouns placed BEFORE it", "都不 = none of them; 不都 = not all of them (partial negation)"), "Putting 都 before the subject (*都我们去).", "Order when combined with 也: Subject + 也 + 都 + Verb.", "What is the difference between 我们都不去 and 我们不都去?", listOf("'None of us are going' vs. 'Not all of us are going'", "They mean the exact same thing", "'We all go' vs. 'We don't go'", "Grammar error in both"), 0),
        GrammarTopic("g_ba", "把 (bǎ)", "Disposal Construction", "Subject + 把 + Object + Verb + Result", 3, "Brings a known, definite direct object before the verb to emphasize how the action disposes of, alters, or relocates it.", "请把门关上。", "請把門關上。", "Qǐng bǎ mén guān shàng.", "Please close the door.", listOf("The object after 把 must be known/specific", "The verb MUST have a result complement, directional complement, or post-verbal element", "Negation (不/没) and modals go BEFORE 把"), "Using a bare verb with nothing after it (*我把书看).", "Use 把 when an action changes the state or position of a specific object.", "Where should 没 (méi) be placed in a 把 sentence?", listOf("Before 把 (他没把作业做完)", "After 把 (*他把没作业做完)", "Before the verb (*他把作业没做完)", "At the end"), 0),
        GrammarTopic("g_bei", "被 (bèi)", "Passive Voice Construction", "Receiver + 被 (+ Agent) + Verb + Complement", 4, "Places the receiver of an action in the subject position to emphasize what happened to it and the resulting state.", "蛋糕被弟弟吃光了。", "蛋糕被弟弟吃光了。", "Dàngāo bèi dìdi chī guāng le.", "The cake was eaten up by my younger brother.", listOf("The agent after 被 can be omitted (蛋糕被吃光了)", "Like 把, the verb requires a result complement or other post-verbal element", "Negation/adverbs go BEFORE 被"), "Omitting the result/complement after the verb.", "把 focuses on what the doer does to the object; 被 focuses on what the receiver undergoes.", "In '我的自行车被偷了', is the agent (the thief) mentioned?", listOf("No — 被 can appear directly before the verb when the agent is unknown/omitted", "Yes — 自行车 is the thief", "It is grammatically invalid without an agent", "被 means 'bought' here"), 0),
        GrammarTopic("g_bi", "比 (bǐ)", "Comparative Construction", "Noun A + 比 + Noun B + Adjective (+ Degree)", 2, "Compares two entities along an adjective or stative verb dimension.", "高铁比公共汽车快得多。", "高鐵比公共汽車快得多。", "Gāotiě bǐ gōnggòng qìchē kuài de duō.", "High-speed rail is much faster than the bus.", listOf("NEVER use 很, 真, or 非常 before the adjective in a 比 sentence", "To express 'even more', use 更 (gèng) before the adjective; for 'much more', put 得多 after the adjective"), "Saying *今天比昨天很热.", "Use A 没有 B (+ 这么/那么) + Adj to say 'A is not as [Adj] as B'.", "Which modifier is allowed BEFORE the adjective in a 比 sentence?", listOf("更 (gèng — even more)", "很 (hěn — very)", "太 (tài — too)", "非常 (fēicháng — extremely)"), 0),
        GrammarTopic("g_yinwei", "因为...所以... (yīnwèi... suǒyǐ...)", "Cause and Effect Connector Pair", "因为 + Cause，所以 + Result", 2, "Explicitly links a reason clause with its consequence clause.", "因为下大雨，所以比赛取消了。", "因為下大雨，所以比賽取消了。", "Yīnwèi xià dàyǔ, suǒyǐ bǐsài qǔxiāo le.", "Because it rained heavily, the match was cancelled.", listOf("Unlike English, Mandarin routinely uses BOTH 'because' (因为) and 'so' (所以) in the same sentence"), "Dropping 所以 when translating mentally from English.", "Provides clear cause-and-effect framing in both spoken and written Chinese.", "In Mandarin, can 因为 and 所以 appear together in the same sentence?", listOf("Yes — pairing 因为...所以... is standard and natural", "No — it is redundant in Chinese", "Only in classical poetry", "Only in questions"), 0),
        GrammarTopic("g_suiran", "虽然...但是... (suīrán... dànshì...)", "Concessive 'Although... Yet/But...'", "虽然 + Concession，但是/可是 + Main Clause", 2, "Concedes a fact in the first clause and introduces an unexpected contrast in the second.", "虽然汉字很难，但是非常有意思。", "雖然漢字很難，但是非常有意思。", "Suīrán Hànzì hěn nán, dànshì fēicháng yǒuyìsi.", "Although Chinese characters are difficult, they are very interesting.", listOf("Must pair 虽然 with 但是, 可是, or 却 in the second clause"), "Using 虽然 alone without a contrasting marker in the second clause.", "Contrast with 尽管...还是... (HSK 4 stronger concession).", "Which word naturally pairs with 虽然 in the second clause?", listOf("但是 (dànshì)", "所以 (suǒyǐ)", "而且 (érqiě)", "如果 (rúguǒ)"), 0)
    )

    val speakingPrompts: List<SpeakingPrompt> = listOf(
        SpeakingPrompt(
            id = "spk_1",
            category = "Pinyin & Tones",
            level = "Stage 0 · Tone Sandhi",
            simplified = "你好，很高兴认识你。",
            traditional = "你好，很高興認識你。",
            pinyin = "Nǐ hǎo, hěn gāoxìng rènshi nǐ.",
            numericTones = listOf(2, 3, 3, 1, 4, 4, 5, 3),
            syllablePinyins = listOf("ní", "hǎo", "hěn", "gāo", "xìng", "rèn", "shi", "nǐ"),
            english = "Hello, nice to meet you.",
            phonemeTip = "Raise 'nǐ' to a 2nd tone (ní) before 'hǎo'. Keep 'gāo' high and flat (55) before dropping sharply on 'xìng' (51). 'shi' is light and neutral.",
            targetPitchContour = listOf(0.45f, 0.78f, 0.30f, 0.22f, 0.50f, 0.88f, 0.88f, 0.85f, 0.25f, 0.80f, 0.22f, 0.45f, 0.28f, 0.55f)
        ),
        SpeakingPrompt(
            id = "spk_2",
            category = "Pinyin & Tones",
            level = "Stage 0 · Four-Tone Drill",
            simplified = "妈妈骑马，马慢，妈妈骂马。",
            traditional = "媽媽騎馬，馬慢，媽媽罵馬。",
            pinyin = "Māma qí mǎ, mǎ màn, māma mà mǎ.",
            numericTones = listOf(1, 5, 2, 3, 3, 4, 1, 5, 4, 3),
            syllablePinyins = listOf("mā", "ma", "qí", "mǎ", "mǎ", "màn", "mā", "ma", "mà", "mǎ"),
            english = "Mother rides a horse; the horse is slow; mother scolds the horse.",
            phonemeTip = "Classic four-tone tongue twister. Contrast high-flat 'mā' (1st), low-dipping 'mǎ' (3rd), and sharp-falling 'mà' & 'màn' (4th). Keep 'qí' aspirated with flat tongue.",
            targetPitchContour = listOf(0.88f, 0.50f, 0.40f, 0.82f, 0.25f, 0.22f, 0.85f, 0.20f, 0.88f, 0.50f, 0.85f, 0.20f, 0.22f, 0.55f)
        ),
        SpeakingPrompt(
            id = "spk_3",
            category = "Daily Phrases",
            level = "HSK 1 · Restaurant",
            simplified = "请问，这里可以刷卡还是用手机支付？",
            traditional = "請問，這裡可以刷卡還是用手機支付？",
            pinyin = "Qǐngwèn, zhèlǐ kěyǐ shuākǎ háishi yòng shǒujī zhīfù?",
            numericTones = listOf(3, 4, 4, 3, 2, 3, 1, 3, 2, 5, 4, 3, 1, 1, 4),
            syllablePinyins = listOf("qǐng", "wèn", "zhè", "lǐ", "kě", "yǐ", "shuā", "kǎ", "hái", "shi", "yòng", "shǒu", "jī", "zhī", "fù"),
            english = "Excuse me, can I swipe a card or pay by mobile phone here?",
            phonemeTip = "Contrast retroflex 'zhè', 'shuā', 'shǒu', 'zhī' (tongue curled back) with palatal 'qǐng', 'jī' (tongue tip down). Notice 'kěyǐ' becomes 'kéyǐ' (2nd + 3rd).",
            targetPitchContour = listOf(0.28f, 0.82f, 0.24f, 0.80f, 0.25f, 0.42f, 0.78f, 0.28f, 0.88f, 0.25f, 0.40f, 0.75f, 0.82f, 0.25f, 0.88f, 0.88f, 0.20f)
        ),
        SpeakingPrompt(
            id = "spk_4",
            category = "Roleplay",
            level = "HSK 3 · Travel & Directions",
            simplified = "麻烦您，去故宫博物院应该在哪一站换乘地铁？",
            traditional = "麻煩您，去故宮博物院應該在哪一站換乘地鐵？",
            pinyin = "Máfan nín, qù Gùgōng Bówùyuàn yīnggāi zài nǎ yí zhàn huànchéng dìtiě?",
            numericTones = listOf(2, 5, 2, 4, 4, 1, 2, 4, 4, 1, 1, 4, 3, 2, 4, 4, 2, 4, 3),
            syllablePinyins = listOf("má", "fan", "nín", "qù", "gù", "gōng", "bó", "wù", "yuàn", "yīng", "gāi", "zài", "nǎ", "yí", "zhàn", "huàn", "chéng", "dì", "tiě"),
            english = "Excuse me, to go to the Palace Museum, at which station should I transfer subways?",
            phonemeTip = "Note the tone sandhi on '一 (yī)' before 4th-tone '站 (zhàn)': pronounce it as rising 2nd tone 'yí zhàn'. Keep 'yīnggāi' smooth and high.",
            targetPitchContour = listOf(0.42f, 0.80f, 0.50f, 0.45f, 0.82f, 0.85f, 0.25f, 0.88f, 0.45f, 0.80f, 0.22f, 0.88f, 0.88f, 0.82f, 0.28f, 0.78f, 0.24f, 0.78f, 0.25f, 0.52f)
        ),
        SpeakingPrompt(
            id = "spk_5",
            category = "HSK Sentences",
            level = "HSK 5 · Workplace & Technology",
            simplified = "养成每天复习生词的习惯，能显著提高汉语表达的流利度。",
            traditional = "養成每天複習生詞的習慣，能顯著提高漢語表達的流利度。",
            pinyin = "Yǎngchéng měitiān fùxí shēngcí de xíguàn, néng xiǎnzhù tígāo Hànyǔ biǎodá de liúlìdù.",
            numericTones = listOf(3, 2, 3, 1, 4, 2, 1, 2, 5, 2, 4, 2, 3, 4, 2, 1, 4, 3, 3, 2, 5, 2, 4, 4),
            syllablePinyins = listOf("yǎng", "chéng", "měi", "tiān", "fù", "xí", "shēng", "cí", "de", "xí", "guàn", "néng", "xiǎn", "zhù", "tí", "gāo", "hàn", "yǔ", "biǎo", "dá", "de", "liú", "lì", "dù"),
            english = "Cultivating the habit of reviewing new words every day can significantly improve fluency in Mandarin expression.",
            phonemeTip = "Maintain natural rhythm by grouping disyllabic words (养成 · 每天 · 复习 · 生词) and keeping structural particle '的 (de)' light and unstressed.",
            targetPitchContour = listOf(0.26f, 0.45f, 0.82f, 0.28f, 0.88f, 0.82f, 0.25f, 0.78f, 0.88f, 0.45f, 0.82f, 0.24f, 0.78f, 0.28f, 0.82f, 0.45f, 0.88f, 0.82f, 0.26f, 0.45f, 0.80f, 0.48f, 0.82f, 0.22f)
        )
    )

    val listeningExercises: List<ListeningExerciseItem> = listOf(
        ListeningExerciseItem("lis_1", "Stage 0 · Minimal Pairs", "Tone Discrimination", "买东西 vs 卖东西", "買東西 vs 賣東西", "mǎi dōngxi (3rd tone) vs mài dōngxi (4th tone)", "Buying things (mǎi) vs Selling things (mài)", "Listen to the audio: '我想去商店买东西。' Is the speaker buying or selling?", listOf("Buying things — 买 (mǎi, 3rd tone)", "Selling things — 卖 (mài, 4th tone)", "Borrowing things — 借 (jiè)", "Looking for things — 找 (zhǎo)"), 0),
        ListeningExerciseItem("lis_2", "Stage 0 · Retroflex vs Palatal", "Tone Discrimination", "十四点四十分", "十四點四十分", "shísì diǎn sìshí fēn", "14:40 (2:40 PM)", "Listen carefully to '十四点四十分'. What time is announced?", listOf("14:40 (Fourteen forty)", "04:14 (Four fourteen)", "10:44 (Ten forty-four)", "04:40 (Four forty)"), 0),
        ListeningExerciseItem("lis_3", "HSK 1 · Dialogue", "Dialogue", "女：你今天下午去哪儿？ 男：我先去银行，然后去超市买水果。", "女：你今天下午去哪兒？ 男：我先去銀行，然後去超市買水果。", "Nǚ: Nǐ jīntiān xiàwǔ qù nǎr? Nán: Wǒ xiān qù yínháng, ránhòu qù chāoshì mǎi shuǐguǒ.", "W: Where are you going this afternoon? M: First I'm going to the bank, then to the supermarket to buy fruit.", "Where is the man going FIRST?", listOf("The bank (银行 yínháng)", "The supermarket (超市 chāoshì)", "The library (图书馆 túshūguǎn)", "The airport (机场 jīchǎng)"), 0),
        ListeningExerciseItem("lis_4", "HSK 2 · Comprehension", "Sentence Comprehension", "虽然今天下雪了，但是地铁还是准时到了。", "雖然今天下雪了，但是地鐵還是準時到了。", "Suīrán jīntiān xià xuě le, dànshì dìtiě háishi zhǔnshí dào le.", "Although it snowed today, the subway still arrived on time.", "What happened to the subway despite the snow?", listOf("It arrived on time (准时到了)", "It was cancelled", "It was delayed by an hour", "It closed early"), 0),
        ListeningExerciseItem("lis_5", "HSK 3 · Dictation & Detail", "Dictation", "请大家把手机调成静音，会议马上就要开始了。", "請大家把手機調成靜音，會議馬上就要開始了。", "Qǐng dàjiā bǎ shǒujī tiáochéng jìngyīn, huìyì mǎshàng jiùyào kāishǐ le.", "Please set your phones to silent mode; the meeting is about to begin immediately.", "What are the attendees asked to do with their phones?", listOf("Set them to silent mode (调成静音)", "Leave them outside the room", "Connect to Wi-Fi", "Use them to take photos"), 0),
        ListeningExerciseItem("lis_6", "HSK 4–5 · News Brief", "Sentence Comprehension", "调查显示，每天坚持阅读三十分钟的人，词汇量增长速度比普通学习者快一倍。", "調查顯示，每天堅持閱讀三十分鐘的人，詞彙量增長速度比普通學習者快一倍。", "Diàochá xiǎnshì, měitiān jiānchí yuèdú sānshí fēnzhōng de rén, cíhuìliàng zēngzhǎng sùdù bǐ pǔtōng xuéxízhě kuài yí bèi.", "Surveys show that people who persist in reading 30 minutes daily grow their vocabulary twice as fast as ordinary learners.", "According to the passage, what habit doubles vocabulary growth speed?", listOf("Reading for 30 minutes every day", "Memorizing 100 words once a week", "Watching movies without subtitles", "Writing essays once a month"), 0)
    )

    val examQuestions: List<HskExamQuestion> = listOf(
        HskExamQuestion("exm_1", 1, "HSK_2", "Listening", "她每天早上七点喝一杯热茶。", "她每天早上七點喝一杯熱茶。", "Tā měitiān zǎoshang qī diǎn hē yì bēi rè chá.", "What does she drink at 7:00 AM every morning?", listOf("A cup of hot tea (一杯热茶)", "A cup of iced coffee", "A glass of milk", "Cold water"), 0, "早上七点 = 7 AM; 喝一杯热茶 = drink a cup of hot tea."),
        HskExamQuestion("exm_2", 1, "HSK_3", "Grammar & Writing", "王老师在大学教我们汉语。", "王老師在大學教我們漢語。", "Wáng lǎoshī zài dàxué jiāo wǒmen Hànyǔ.", "Which word order correctly expresses 'Teacher Wang teaches us Chinese at the university'?", listOf("王老师在大学教我们汉语。", "王老师教我们汉语在大学。", "在大学王老师汉语教我们。", "我们教王老师在大学汉语。"), 0, "In Mandarin, location prepositional phrases (在大学) precede the main verb (教)."),
        HskExamQuestion("exm_3", 2, "HSK_2", "Reading", "这本书我已经看完了，借给你看吧。", "這本書我已經看完了，借給你吧。", "Zhè běn shū wǒ yǐjīng kàn wán le, jiè gěi nǐ kàn ba.", "Why is the speaker offering the book to the listener?", listOf("Because the speaker has already finished reading it (看完了)", "Because the book is too difficult", "Because the speaker bought two copies", "Because the library is closed"), 0, "看完 (kàn wán) uses the result complement 完 (finish) to show the speaker completed reading the book."),
        HskExamQuestion("exm_4", 3, "HSK_2", "Grammar & Writing", "你出门的时候，别忘了把窗户关好。", "你出門的時候，別忘了把窗戶關好。", "Nǐ chūmén de shíhou, bié wàng le bǎ chuānghu guān hǎo.", "Choose the correct particle to complete: '别忘了 ___ 窗户关好'", listOf("把 (bǎ)", "被 (bèi)", "比 (bǐ)", "得 (de)"), 0, "The speaker instructs the listener to perform a disposal action on a specific object (窗户, window) with result complement 好."),
        HskExamQuestion("exm_5", 4, "HSK_3", "Reading", "真正的自律并不是偶尔一次的熬夜努力，而是日复一日的坚持。", "真正的自律並不是偶爾一次的熬夜努力，而是日復一日的堅持。", "Zhēnzhèng de zìlǜ bìng bú shì ǒu'ěr yí cì de áoyè nǔlì, érshì rìfùyírì de jiānchí.", "What grammatical connector pair frames this sentence?", listOf("不是...而是... (Not A, but rather B)", "虽然...但是... (Although A, B)", "不仅...而且... (Not only A, but also B)", "即使...也... (Even if A, still B)"), 0, "并不是 A，而是 B contrasts a misconception (occasional all-nighters) with the true definition (day-after-day persistence)."),
        HskExamQuestion("exm_6", 5, "HSK_2", "Grammar & Writing", "面对突发状况，他表现得十分镇定。", "面對突發狀況，他表現得十分鎮定。", "Miànduì tūfā zhuàngkuàng, tā biǎoxiàn de shífēn zhèndìng.", "Why is 得 (de) used in '表现得十分镇定'?", listOf("It introduces a degree/manner complement after the verb 表现", "It modifies a noun like 的", "It indicates past tense", "It marks passive voice"), 0, "Verb + 得 (de) + Descriptive Complement evaluates how an action was performed (acted very calmly)."),
        HskExamQuestion("exm_7", 6, "HSK_3", "Reading", "学习任何一门技艺都不能急于求成，唯有循序渐进，方能厚积薄发。", "學習任何一門技藝都不能急於求成，唯有循序漸進，方能厚積薄發。", "Xuéxí rènhé yì mén jìyì dōu bù néng jíyú-qiúchéng, wéiyǒu xúnxù-jiànjìn, fāng néng hòujī-bófā.", "What does the idiom '循序渐进 (xúnxù-jiànjìn)' mean in this context?", listOf("To advance step by step in proper sequence", "To give up halfway", "To rush for quick results", "To work in isolation"), 0, "循序渐进 contrasts with 急于求成 (impatient for quick success) and means progressing methodically step by step.")
    )

    val milestoneBadges: List<MilestoneBadge> = listOf(
        MilestoneBadge("badge_first_step", "First Ink", "始", "Complete your first Mandarin lesson or onboarding.", 50),
        MilestoneBadge("badge_pinyin_starter", "Tone Architect", "音", "Practice Pinyin initials, finals, and tone sandhi.", 120),
        MilestoneBadge("badge_hanzi_scribe", "Calligrapher", "字", "Practice stroke-order character writing in Hanzi Studio.", 200),
        MilestoneBadge("badge_srs_scholar", "Memory Seal", "记", "Complete spaced repetition flashcard reviews.", 300),
        MilestoneBadge("badge_voice_flow", "Pitch Master", "声", "Complete a pitch-contour pronunciation analysis in Speaking Center.", 450),
        MilestoneBadge("badge_hsk_adept", "HSK Scholar", "达", "Surpass 600 cumulative mastery points across HSK modules.", 600)
    )

    val offlinePacks: List<OfflineContentPack> = listOf(
        OfflineContentPack("pack_pinyin", "Stage 0 · Complete Pinyin & Tone Audio Pack", "Vocabulary & Audio", "Stage 0", "21 Initials, 36 Finals, Tone Pairs & Native TTS Cache", 14.2f, "Offline phonetic tables, minimal-pair audio drills, and tone sandhi rules."),
        OfflineContentPack("pack_hsk1", "HSK 1 · Core Foundation Pack (2.0 & 3.0)", "Curriculum", "HSK 1", "500 Words, 300 Characters, 15 Lessons", 22.8f, "Complete beginner survival Mandarin lessons, flashcards, and stroke animations."),
        OfflineContentPack("pack_hsk2", "HSK 2 · Daily Conversational Pack", "Curriculum", "HSK 2", "772 Words, Comparisons, Weather & Transport", 28.5f, "Offline dialogues, listening comprehension tracks, and grammar drills."),
        OfflineContentPack("pack_hsk3", "HSK 3 · Intermediate Fluency & 把 Structure", "Curriculum", "HSK 3", "973 Words, Complements & Reading Passages", 36.4f, "Intermediate lessons, character stroke sets, and mock HSK 3 exam papers."),
        OfflineContentPack("pack_grammar", "Complete Mandarin Grammar & Stroke Studio", "Grammar & Characters", "HSK 1–6", "17 Core Patterns + Interactive Stroke Vectors", 19.6f, "Full offline grammar reference library and vector stroke order database."),
        OfflineContentPack("pack_hsk4_6", "HSK 4–6 · Advanced Literary & News Corpus", "Curriculum", "HSK 4–6", "Formal Register, Chengyu Idioms & Mock Exams", 54.0f, "Advanced reading passages, formal connectors, and HSK 4–6 timed exams.")
    )
}
