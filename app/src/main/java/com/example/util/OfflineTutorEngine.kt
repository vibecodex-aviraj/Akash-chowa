package com.example.util

import com.example.data.repository.BengaliKnowledgeBase
import com.example.data.repository.BengaliQuizData
import java.util.Locale

object OfflineTutorEngine {

    /**
     * Answers student questions completely offline using local embedded Bengali curriculum knowledge.
     */
    fun answerOffline(query: String): String {
        val q = query.lowercase(Locale.ROOT).trim()

        // 1. Detect if student specified a word limit
        val wordLimitNote = when {
            q.contains("২০ শব্দ") || q.contains("20 শব্দ") -> " (উত্তর সংক্ষেপ: প্রায় ২০ শব্দে ১ নম্বরের মান)"
            q.contains("৫০ শব্দ") || q.contains("50 শব্দ") || q.contains("৩ নম্বর") -> " (উত্তর সংক্ষেপ: প্রায় ৫০ শব্দে ৩ নম্বরের মান)"
            q.contains("১০০ শব্দ") || q.contains("100 শব্দ") || q.contains("১৫০ শব্দ") || q.contains("150 শব্দ") || q.contains("৫ নম্বর") -> " (উত্তর সংক্ষেপ: প্রায় ১০০-১৫০ শব্দে ৫ নম্বরের মান)"
            q.contains("২৫০ শব্দ") || q.contains("250 শব্দ") || q.contains("প্রবন্ধ") || q.contains("বিশদ") -> " (বিশদ রচনা মান)"
            else -> ""
        }

        // Match against chapters and topics
        val matchedChapter = BengaliKnowledgeBase.syllabusItems.firstOrNull { item ->
            q.contains(item.title.lowercase(Locale.ROOT)) ||
                    q.contains(item.author.lowercase(Locale.ROOT).split(" ").lastOrNull() ?: "")
        }

        val prefix = "📱 [অফলাইন মোড — সংরক্ষিত তথ্যভাণ্ডার]$wordLimitNote\n\n"

        if (matchedChapter != null) {
            val relatedQuestions = matchedChapter.keyQuestions.joinToString("\n") { "• $it" }
            return buildString {
                append(prefix)
                append("📖 **${matchedChapter.title}** (${matchedChapter.category})\n")
                append("লেখক/কবি: ${matchedChapter.author}\n\n")
                append("📌 **বিষয়বস্তুর সারসংক্ষেপ:**\n")
                append(matchedChapter.summary)
                append("\n\n")
                append("📝 **CBSE ক্লাস ৯ পরীক্ষার সম্ভাব্য গুরুত্বপূর্ণ প্রশ্নোত্তর:**\n")
                append(relatedQuestions)
                append("\n\n")
                append("💡 *অফলাইনে পূর্ণাঙ্গ সিলেবাস সহায়তা প্রদান করা হয়েছে। বিস্তারিত ব্যাখ্যার জন্য অনলাইন মোডেও প্রশ্ন করতে পারেন।*")
            }
        }

        // Specific topics: Aam Aantir Bhepu characters
        if (q.contains("অপু") || q.contains("দুর্গা") || q.contains("আম আঁটির ভেঁপু") || q.contains("হরিহর") || q.contains("সর্বজয়া")) {
            return buildString {
                append(prefix)
                append("🍈 **'আম আঁটির ভেঁপু' — বিভূতিভূষণ বন্দ্যোপাধ্যায়**\n\n")
                append("• **মূল চরিত্রসমূহ:**\n")
                append("  ১. **অপু:** নিশ্চিন্দিপুর গ্রামের পুরোহিত হরিহর রায়ের স্বপ্নদর্শী ছেলে। মহাভারত পড়তে এবং প্রকৃতি পর্যবেক্ষণ করতে ভালোবাসে। তার প্রিয় খেলনা চার পয়সা দামের 'আম আঁটির ভেঁপু'।\n")
                append("  ২. **দুর্গা:** অপুর চঞ্চল, স্নেহময়ী বড় দিদি। মেঠোপথে আম কুড়ানো, বনভোজন ও অপুকে অপত্য স্নেহে আগলে রাখাই তার আনন্দ। দুর্ভাগ্যজনকভাবে ম্যালেরিয়া জ্বরে তার মৃত্যু ঘটে।\n")
                append("  ৩. **হরিহর রায়:** অপুর পিতা, শান্ত ও আশাবাদী দরিদ্র ব্রাহ্মণ। অর্থোপার্জনের আশায় বাইরে ঘুরে বেড়ান।\n")
                append("  ৪. **সর্বজয়া:** অপুর মা, দারিদ্র্য ও সাংসারিক কষ্টে জর্জরিত কিন্তু সন্তানদের সুরক্ষায় সদা সচেষ্ট।\n\n")
                append("• **উপন্যাসের মূল সুর:** গ্রামীণ বাংলার নিটোল সৌন্দর্য, পল্লীজীবনের অফুরন্ত আনন্দ ও সীমাহীন দারিদ্র্যের বাস্তব চিত্র। পরিশেষে শোকাহত হরিহর পরিবার গ্রাম ত্যাগ করে কাশীযাত্রা করে।")
            }
        }

        // Specific topics: Grammar (ধ্বনি পরিবর্তন, সন্ধি, শব্দ ও পদ)
        if (q.contains("ধ্বনি পরিবর্তন") || q.contains("স্বরভক্তি") || q.contains("অপিনিহিতি") || q.contains("অভিশ্রুতি") || q.contains("ব্যাকরণ") || q.contains("সন্ধি")) {
            return buildString {
                append(prefix)
                append("📚 **বাংলা ব্যাকরণ — ক্লাস ৯ CBSE নির্দেশিকা**\n\n")
                append("১. **স্বরভক্তি (বিপ্রকর্ষ):** উচ্চারণের সুবিধার্থে দুটি যুক্তব্যঞ্জনের মাঝে স্বরধ্বনির আগমন।\n")
                append("   • উদাহরণ: রত্ন > রতন (অ-এর আগমন), ধর্ম > ধরম, প্রীতি > পিরীতি।\n\n")
                append("২. **অপিনিহিতি:** শব্দের মধ্যস্থ 'ই' বা 'উ' কার নির্দিষ্ট অবস্থানের আগেই উচ্চারিত হওয়ার রীতি।\n")
                append("   • উদাহরণ: আজি > আইজ, সাধু > সাউধ, রাখিয়া > রাইখ্যা।\n\n")
                append("৩. **অভিশ্রুতি:** অপিনিহিতির ফলে এগিয়ে আসা 'ই' বা 'উ' পূর্ববর্তী স্বরধ্বনিকে প্রভাবিত করে সংকুচিত হলে তাকে অভিশ্রুতি বলে।\n")
                append("   • উদাহরণ: করিয়া > কইরা > করে; রাখিয়া > রাইখ্যা > রেখে।\n\n")
                append("৪. **সন্ধি বিচ্ছেদ:** পরস্পর সন্নিহিত দুটি ধ্বনির মিলনকে সন্ধি বলে। যেমন: বিদ্যালয় = বিদ্যা + আলয়; পরীক্ষা = পরি + ঈক্ষা।")
            }
        }

        // Specific topics: Project / Bhavsamprasaran
        if (q.contains("প্রকল্প") || q.contains("প্রজেক্ট") || q.contains("ভাবসম্প্রসারণ") || q.contains("প্রতিবেদন")) {
            return buildString {
                append(prefix)
                append("📝 **CBSE ক্লাস ৯ বাংলা প্রজেক্ট ও নির্মিতি সহায়িকা**\n\n")
                append("• **ভাবসম্প্রসারণের নিয়ম:**\n")
                append("  ১. প্রদত্ত নীতিবাক্য বা কবিতার চরণটি মনোযোগ দিয়ে পড়ে মূল ভাব উপলব্ধি করতে হবে।\n")
                append("  ২. কোনো উদাহরণ বা উপমার আশ্রয় নিয়ে ভাবটিকে প্রাঞ্জল ভাষায় বিস্তার করতে হবে (১০০-১৫০ শব্দ)।\n")
                append("  ৩. নিজস্ব মন্তব্য বা উপসংহার দিয়ে শেষ করতে হবে।\n\n")
                append("• **জনপ্রিয় ভাবসম্প্রসারণ:**\n")
                append("  'স্বদেশের উপকারে নাই যার মন / কে বলে মানুষ তারে পশু সেই জন।'\n")
                append("  'অন্যায় যে করে আর অন্যায় যে সহে / তব ঘৃণা তারে যেন তৃণসম দহে।'\n\n")
                append("• **প্রজেক্ট খাতা তৈরি:** ভূমিকা, বিষয় নির্বাচন, তথ্য ও চিত্র সংগ্রহ, বিশ্লেষণ ও কৃতজ্ঞতা স্বীকার সমন্বয়ে তৈরি করতে হয়।")
            }
        }

        // Default curriculum overview
        return buildString {
            append(prefix)
            append("নমস্কার! আপনি বর্তমানে **অফলাইন মোডে** আছেন।\n\n")
            append("আমি ইন্টারনেট সংযোগ ছাড়াই ক্লাস ৯ বাংলা CBSE সিলেবাসের নিম্নলিখিত বিষয়গুলিতে সম্পূর্ণ সাহায্য করতে পারি:\n")
            append("• **সাহিত্য সঞ্চয়ন:** দাম, কলিঙ্গদেশে ঝড়-বৃষ্টি, ধীবর-বৃত্তান্ত, ইলিয়াস, আবহমান, চিঠি, ভাঙার গান, খেয়া, প্রফেসর শঙ্কু।\n")
            append("• **আম আঁটির ভেঁপু:** অপু ও দুর্গার শৈশব, বনভোজন দৃশ্য, পটেশ্বরী দেবীর মেলা, দুর্গার মৃত্যু, কাশীযাত্রা।\n")
            append("• **বাংলা ব্যাকরণ:** ধ্বনি পরিবর্তন (স্বরভক্তি, অপিনিহিতি, অভিশ্রুতি), সন্ধি বিচ্ছেদ, শব্দ ও পদ।\n")
            append("• **নির্মিতি ও প্রজেক্ট:** ভাবসম্প্রসারণ, রচনা, প্রকল্প গাইড।\n\n")
            append("যেকোনো নির্দিষ্ট অধ্যায় বা চরিত্রের নাম লিখে প্রশ্ন করুন, অফলাইনে তাৎক্ষণিক উত্তর পাবেন!")
        }
    }
}
