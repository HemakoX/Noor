package com.example.drziadandroid.data.local

import android.content.Context
import com.example.drziadandroid.data.model.Quote
import com.example.drziadandroid.data.model.WidgetPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class LocalQuoteStore(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val quotesFile = File(context.filesDir, "quotely_quotes.json")
    private val prefsFile = File(context.filesDir, "quotely_prefs.json")

    private val lock = Any()

    init {
        synchronized(lock) {
            if (!quotesFile.exists() || quotesFile.length() == 0L) {
                saveQuotesInternal(preseededQuotes)
            } else {
                // Check if store needs upgrade/sync with preseeded Islamic items
                try {
                    val existing = getAllQuotes().toMutableList()
                    val existingIds = existing.map { it.id }.toSet()
                    var added = false
                    for (p in preseededQuotes) {
                        if (!existingIds.contains(p.id) && (p.category == "Quran" || p.category == "Hadith")) {
                            existing.add(p)
                            added = true
                        }
                    }
                    if (added) {
                        saveQuotesInternal(existing)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (!prefsFile.exists() || prefsFile.length() == 0L) {
                savePreferencesInternal(WidgetPreferences())
            }
        }
    }

    fun getAllQuotes(): List<Quote> = synchronized(lock) {
        try {
            if (!quotesFile.exists()) return preseededQuotes
            val text = quotesFile.readText()
            if (text.isBlank()) return preseededQuotes
            json.decodeFromString<List<Quote>>(text)
        } catch (e: Exception) {
            preseededQuotes
        }
    }

    fun saveQuotes(quotes: List<Quote>) = synchronized(lock) {
        saveQuotesInternal(quotes)
    }

    private fun saveQuotesInternal(quotes: List<Quote>) {
        try {
            val text = json.encodeToString(quotes)
            quotesFile.writeText(text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getPreferences(): WidgetPreferences = synchronized(lock) {
        try {
            if (!prefsFile.exists()) return WidgetPreferences()
            val text = prefsFile.readText()
            if (text.isBlank()) return WidgetPreferences()
            json.decodeFromString<WidgetPreferences>(text)
        } catch (e: Exception) {
            WidgetPreferences()
        }
    }

    fun savePreferences(prefs: WidgetPreferences) = synchronized(lock) {
        savePreferencesInternal(prefs)
    }

    private fun savePreferencesInternal(prefs: WidgetPreferences) {
        try {
            val text = json.encodeToString(prefs)
            prefsFile.writeText(text)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun addQuotes(newQuotes: List<Quote>): Int = synchronized(lock) {
        val existing = getAllQuotes().toMutableList()
        val existingTexts = existing.map { it.text.lowercase().trim() }.toSet()
        var addedCount = 0
        for (q in newQuotes) {
            val normalized = q.text.lowercase().trim()
            if (normalized.isNotBlank() && !existingTexts.contains(normalized)) {
                existing.add(q)
                addedCount++
            }
        }
        if (addedCount > 0) {
            saveQuotesInternal(existing)
        }
        return addedCount
    }

    companion object {
        val preseededQuotes = listOf(
            // Islamic Mode: Authentic Quranic Verses (Ayat)
            Quote("q1", "So remember Me; I will remember you.", "سورة البقرة [2:152] • Surah Al-Baqarah [2:152]", "Quran", "فَاذْكُرُونِي أَذْكُرْكُمْ"),
            Quote("q2", "For indeed, with hardship will come ease.", "سورة الشرح [94:5] • Surah Ash-Sharh [94:5]", "Quran", "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا"),
            Quote("q3", "Do not weaken and do not grieve.", "سورة آل عمران [3:139] • Surah Al-Imran [3:139]", "Quran", "وَلَا تَهِنُوا وَلَا تَحْزَنُوا"),
            Quote("q4", "Unquestionably, by the remembrance of Allah do hearts find peace.", "سورة الرعد [13:28] • Surah Ar-Ra'd [13:28]", "Quran", "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ"),
            Quote("q5", "Allah does not charge a soul except with that within its capacity.", "سورة البقرة [2:286] • Surah Al-Baqarah [2:286]", "Quran", "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا"),
            Quote("q6", "And whoever relies upon Allah - then He is sufficient for him.", "سورة الطلاق [65:3] • Surah At-Talaq [65:3]", "Quran", "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ"),
            Quote("q7", "If you are grateful, I will surely increase you in favor.", "سورة إبراهيم [14:7] • Surah Ibrahim [14:7]", "Quran", "لَئِن شَكَرْتُمْ لَأَزِيدَنَّكُمْ"),
            Quote("q8", "They plan, and Allah plans. And Allah is the best of planners.", "سورة الأنفال [8:30] • Surah Al-Anfal [8:30]", "Quran", "وَيَمْكُرُونَ وَيَمْكُرُ اللَّهُ ۖ وَاللَّهُ خَيْرُ الْمَاكِرِينَ"),
            Quote("q9", "When My servants ask you concerning Me, indeed I am near.", "سورة البقرة [2:186] • Surah Al-Baqarah [2:186]", "Quran", "وَإِذَا سَأَلَكَ عِبَادِي عَنِّي فَإِنِّي قَرِيبٌ"),
            Quote("q10", "Indeed, Allah is with the patient.", "سورة البقرة [2:153] • Surah Al-Baqarah [2:153]", "Quran", "إِنَّ اللَّهَ مَعَ الصَّابِرِينَ"),
            Quote("q11", "There is no deity except You; exalted are You.", "سورة الأنبياء [21:87] • Surah Al-Anbya [21:87]", "Quran", "لَّا إِلَٰهَ إِلَّا أَنتَ سُبْحَانَكَ"),
            Quote("q12", "It is You we worship and You we ask for help.", "سورة الفاتحة [1:5] • Surah Al-Fatiha [1:5]", "Quran", "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ"),
            Quote("q13", "My Lord, expand for me my breast and ease for me my task.", "سورة طه [20:25-26] • Surah Taha [20:25-26]", "Quran", "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي"),
            Quote("q14", "Say, 'He is Allah, [who is] One, Allah, the Eternal Refuge.'", "سورة الإخلاص [112:1-2] • Surah Al-Ikhlas [112:1-2]", "Quran", "قُلْ هُوَ اللَّهُ أَحَدٌ • اللَّهُ الصَّمَدُ"),
            Quote("q15", "And My Mercy encompasses all things.", "سورة الأعراف [7:156] • Surah Al-A'raf [7:156]", "Quran", "وَرَحْمَتِي وَسِعَتْ كُلَّ شَيْءٍ"),

            // Islamic Mode: Authentic Hadiths
            Quote("h1", "Actions are judged by intentions.", "صحيح البخاري [1] • Sahih Al-Bukhari [1]", "Hadith", "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ"),
            Quote("h2", "Purity is half of faith.", "صحيح مسلم [223] • Sahih Muslim [223]", "Hadith", "الطَّهُورُ شَطْرُ الإِيمَانِ"),
            Quote("h3", "A Muslim is one from whose tongue and hand other Muslims are safe.", "صحيح البخاري [6018] • Sahih Al-Bukhari [6018]", "Hadith", "الْمُسْلِمُ مَنْ سَلِمَ الْمُسْلِمُونَ مِنْ لِسَانِهِ وَيَدِهِ"),
            Quote("h4", "Be mindful of Allah, and He will protect you.", "سنن الترمذي [2516] • Sunan At-Tirmidhi [2516]", "Hadith", "احْفَظِ اللَّهَ يَحْفَظْكَ"),
            Quote("h5", "Whoever believes in Allah and the Last Day should speak good or remain silent.", "صحيح البخاري [6011] • Sahih Al-Bukhari [6011]", "Hadith", "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ"),
            Quote("h6", "Your smiling in the face of your brother is charity.", "سنن الترمذي [1956] • Sunan At-Tirmidhi [1956]", "Hadith", "تَبَسُّمُكَ فِي وَجْهِ أَخِيكَ صَدَقَةٌ"),
            Quote("h7", "Allah will not show mercy to those who do not show mercy to people.", "صحيح البخاري [6010] • Sahih Al-Bukhari [6010]", "Hadith", "لاَ يَرْحَمُ اللَّهُ مَنْ لاَ يَرْحَمُ النَّاسَ"),
            Quote("h8", "The best among you are those who learn the Quran and teach it.", "صحيح البخاري [5027] • Sahih Al-Bukhari [5027]", "Hadith", "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ"),
            Quote("h9", "The example of one who remembers his Lord vs one who does not is like the living and the dead.", "صحيح البخاري [6407] • Sahih Al-Bukhari [6407]", "Hadith", "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لاَ يَذْكُرُ رَبَّهِ مَثَلُ الْحَيِّ وَالْمَيِّتِ"),
            Quote("h10", "Whoever takes a path seeking knowledge, Allah makes smooth for him a path to Paradise.", "صحيح مسلم [2699] • Sahih Muslim [2699]", "Hadith", "مَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ"),
            Quote("h11", "Make things easy for people and not difficult.", "صحيح البخاري [69] • Sahih Al-Bukhari [69]", "Hadith", "يَسِّرُوا وَلاَ تُعَسِّرُوا وَبَشِّرُوا وَلاَ تُنَفِّرُوا"),
            Quote("h12", "None of you truly believes until he loves for his brother what he loves for himself.", "صحيح البخاري [13] • Sahih Al-Bukhari [13]", "Hadith", "لاَ يُؤْمِنُ أَحَدُكُمْ حَتَّى يُحِبَّ لأَخِيهِ مَا يُحِبُّ لِنَفْسِهِ"),

            // Motivation
            Quote("p1", "The only way to do great work is to love what you do.", "Steve Jobs", "Motivation"),
            Quote("p2", "It always seems impossible until it's done.", "Nelson Mandela", "Motivation"),
            Quote("p3", "Don't watch the clock; do what it does. Keep going.", "Sam Levenson", "Motivation"),
            Quote("p4", "Believe you can and you're halfway there.", "Theodore Roosevelt", "Motivation"),
            Quote("p5", "Success is not final, failure is not fatal: it is the courage to continue that counts.", "Winston Churchill", "Motivation"),
            Quote("p6", "Start where you are. Use what you have. Do what you can.", "Arthur Ashe", "Motivation"),
            Quote("p7", "Act as if what you do makes a difference. It does.", "William James", "Motivation"),

            // Philosophy
            Quote("p8", "He who has a why to live can bear almost any how.", "Friedrich Nietzsche", "Philosophy"),
            Quote("p9", "We are what we repeatedly do. Excellence, then, is not an act, but a habit.", "Aristotle", "Philosophy"),
            Quote("p10", "The unexamined life is not worth living.", "Socrates", "Philosophy"),
            Quote("p11", "Happiness depends upon ourselves.", "Aristotle", "Philosophy"),
            Quote("p12", "Waste no more time arguing about what a good man should be. Be one.", "Marcus Aurelius", "Philosophy"),
            Quote("p13", "The soul becomes dyed with the color of its thoughts.", "Marcus Aurelius", "Philosophy"),
            Quote("p14", "Freedom is the only worthy goal in life. It is won by disregarding things that lie beyond our control.", "Epictetus", "Philosophy"),

            // Science
            Quote("p15", "Somewhere, something incredible is waiting to be known.", "Carl Sagan", "Science"),
            Quote("p16", "Science is a way of thinking much more than it is a body of knowledge.", "Carl Sagan", "Science"),
            Quote("p17", "Nothing in life is to be feared, it is only to be understood.", "Marie Curie", "Science"),
            Quote("p18", "Equipped with his five senses, man explores the universe around him and calls the adventure Science.", "Edwin Hubble", "Science"),
            Quote("p19", "Look deep into nature, and then you will understand everything better.", "Albert Einstein", "Science"),
            Quote("p20", "The important thing is not to stop questioning. Curiosity has its own reason for existing.", "Albert Einstein", "Science"),
            Quote("p21", "Science is the poetry of reality.", "Richard Dawkins", "Science"),

            // Life
            Quote("p22", "In three words I can sum up everything I've learned about life: it goes on.", "Robert Frost", "Life"),
            Quote("p23", "Life is what happens when you're busy making other plans.", "John Lennon", "Life"),
            Quote("p24", "To live is the rarest thing in the world. Most people exist, that is all.", "Oscar Wilde", "Life"),
            Quote("p25", "Life is either a daring adventure or nothing at all.", "Helen Keller", "Life"),
            Quote("p26", "Good timber does not grow with ease: the stronger wind, the stronger trees.", "J. Willard Marriott", "Life"),
            Quote("p27", "The purpose of life is not to be happy. It is to be useful, to be honorable, to be compassionate.", "Ralph Waldo Emerson", "Life"),
            Quote("p28", "Turn your wounds into wisdom.", "Oprah Winfrey", "Life"),

            // Wisdom
            Quote("p29", "Knowing others is intelligence; knowing yourself is true wisdom.", "Lao Tzu", "Wisdom"),
            Quote("p30", "The fool thinks he is wise, but the wise man knows himself to be a fool.", "William Shakespeare", "Wisdom"),
            Quote("p31", "Turn your face to the sun and the shadows fall behind you.", "Maori Proverb", "Wisdom"),
            Quote("p32", "Silence is a source of Great Strength.", "Lao Tzu", "Wisdom"),
            Quote("p33", "By three methods we may learn wisdom: First, by reflection, which is noblest; Second, by imitation; and third by experience.", "Confucius", "Wisdom"),
            Quote("p34", "Wisdom begins in wonder.", "Socrates", "Wisdom"),
            Quote("p35", "Honesty is the first chapter in the book of wisdom.", "Thomas Jefferson", "Wisdom"),

            // Success
            Quote("p36", "Try not to become a man of success. Rather become a man of value.", "Albert Einstein", "Success"),
            Quote("p37", "Success is walking from failure to failure with no loss of enthusiasm.", "Winston Churchill", "Success"),
            Quote("p38", "The secret of success is to do the common thing uncommonly well.", "John D. Rockefeller Jr.", "Success"),
            Quote("p39", "Success usually comes to those who are too busy to be looking for it.", "Henry David Thoreau", "Success"),
            Quote("p40", "Opportunities don't happen. You create them.", "Chris Grosser", "Success"),
            Quote("p41", "There are no secrets to success. It is the result of preparation, hard work, and learning from failure.", "Colin Powell", "Success"),

            // Humor
            Quote("p42", "I am so clever that sometimes I don't understand a single word of what I am saying.", "Oscar Wilde", "Humor"),
            Quote("p43", "Always remember that you are absolutely unique. Just like everyone else.", "Margaret Mead", "Humor"),
            Quote("p44", "I can resist everything except temptation.", "Oscar Wilde", "Humor"),
            Quote("p45", "A day without sunshine is like, you know, night.", "Steve Martin", "Humor"),
            Quote("p46", "People say nothing is impossible, but I do nothing every day.", "A.A. Milne", "Humor"),
            Quote("p47", "I'm not superstitious, but I am a little sticious.", "Michael Scott", "Humor"),

            // Art
            Quote("p48", "Every child is an artist. The problem is how to remain an artist once he grows up.", "Pablo Picasso", "Art"),
            Quote("p49", "Art enables us to find ourselves and lose ourselves at the same time.", "Thomas Merton", "Art"),
            Quote("p50", "Creativity takes courage.", "Henri Matisse", "Art"),
            Quote("p51", "Design is not just what it looks like and feels like. Design is how it works.", "Steve Jobs", "Art"),
            Quote("p52", "Art is the lie that enables us to realize the truth.", "Pablo Picasso", "Art"),

            // Tech
            Quote("p53", "Any sufficiently advanced technology is indistinguishable from magic.", "Arthur C. Clarke", "Tech"),
            Quote("p54", "Technology is best when it brings people together.", "Matt Mullenweg", "Tech"),
            Quote("p55", "The computer was born to solve problems that did not exist before.", "Bill Gates", "Tech"),
            Quote("p56", "Hardware: The parts of a computer system that can be kicked.", "Jeff Pesis", "Tech"),
            Quote("p57", "Simplicity is prerequisite for reliability.", "Edsger W. Dijkstra", "Tech")
        )
    }
}
