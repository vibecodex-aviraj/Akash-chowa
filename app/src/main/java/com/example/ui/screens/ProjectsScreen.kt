package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ProjectGuide(
    val title: String,
    val bookRef: String,
    val description: String,
    val steps: List<String>,
    val prompt: String
)

@Composable
fun ProjectsScreen(
    onDraftProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val projectGuides = listOf(
        ProjectGuide(
            title = "আম আঁটির ভেঁপু উপন্যাসে গ্রামবাংলার প্রকৃতি ও অপুর শৈশব",
            bookRef = "সহায়ক গ্রন্থ: আম আঁটির ভেঁপু — বিভূতিভূষণ বন্দ্যোপাধ্যায়",
            description = "নিশ্চিন্দিপুর গ্রামের গাছপালা, মেঠোপথ, নদী ও বৃষ্টির রূপ। অপু এবং দুর্গার বাল্যজীবনের রোমাঞ্চ, ফল কুড়ানো ও চড়ুইভাতির স্মৃতিচারণ।",
            steps = listOf(
                "১) ভূমিকা: বিভূতিভূষণের সাহিত্যিক দৃষ্টিভঙ্গি ও উপন্যাসটির সংক্ষিপ্ত পটভূমি।",
                "২) প্রকৃতি ও পল্লীজীবন: নিশ্চিন্দিপুর গ্রামের নিখুঁত চিত্রায়ন।",
                "৩) অপুর কল্পনাপ্রবণ চরিত্র ও টিনের বাক্সের রহস্য।",
                "৪) দুর্গার স্নেহ, চঞ্চলতা ও অকালমৃত্যুর শোকাবহ প্রভাব।",
                "৫) ব্যক্তিগত উপলব্ধি, উপসংহার ও কৃতজ্ঞতা স্বীকার।"
            ),
            prompt = "ক্লাস ৯ সিবিএসই পরীক্ষার জন্য 'আম আঁটির ভেঁপু উপন্যাসে গ্রামবাংলার প্রকৃতি ও অপুর শৈশব' বিষয়ের ওপর একটি পূর্ণাঙ্গ প্রজেক্ট তৈরি করে দাও (ভূমিকা, পয়েন্টভিত্তিক বিশ্লেষণ ও উপসংহারসহ)।"
        ),
        ProjectGuide(
            title = "সাহিত্য সঞ্চয়নে মানবিক মূল্যবোধ ও শিক্ষকের মর্যাদা ('দাম' গল্পের আলোকে)",
            bookRef = "পাঠ্যপুস্তক: সাহিত্য সঞ্চয়ন — নারায়ণ গঙ্গোপাধ্যায়",
            description = "ছাত্র ও শিক্ষকের মধ্যকার শাশ্বত স্নেহের সম্পর্ক। শাসন ও স্নেহের দ্বন্দ্ব এবং পরিণত বয়সে সুকুমারের আত্মোপলব্ধি।",
            steps = listOf(
                "১) ভূমিকা: ছাত্র-শিক্ষক সম্পর্কের আধুনিক ও সনাতন ধারণা।",
                "২) 'দাম' গল্পের কেন্দ্রীয় চরিত্র— অঙ্কের মাস্টারমশাই ও সুকুমার।",
                "৩) বাল্যকালের ভীতি বনাম পরিণত বয়সের শ্রদ্ধাবোধ।",
                "৪) স্নেহের মূল্য কী অর্থ দিয়ে পরিমাপ করা সম্ভব? — দার্শনিক বিশ্লেষণ।",
                "৫) বর্তমান যুগে এই গল্পের প্রাসঙ্গিকতা ও উপসংহার।"
            ),
            prompt = "'সাহিত্য সঞ্চয়ন' গ্রন্থের 'দাম' গল্পের আলোকে ছাত্র-শিক্ষক সম্পর্ক ও আত্মোপলব্ধি নিয়ে ক্লাস ৯ এর জন্য একটি প্রজেক্টের সম্পূর্ণ খসড়া প্রস্তুত করে দাও।"
        ),
        ProjectGuide(
            title = "বাংলা ভাষার বৈচিত্র্য ও ব্যাকরণগত সৌন্দর্য (ধ্বনি পরিবর্তন ও সন্ধি)",
            bookRef = "CBSE ক্লাস ৯ ব্যাকরণ অংশ",
            description = "বাংলা ভাষা কীভাবে যুগের সাথে সাথে ধ্বনি পরিবর্তনের মধ্য দিয়ে আজকের রূপ ধারণ করেছে। প্রাত্যহিক জীবনের কথোপকথনে স্বরভক্তি, অপিনিহিতি ও সমীভবনের ব্যবহার।",
            steps = listOf(
                "১) বাংলা ভাষার ক্রমবিকাশের সংক্ষিপ্ত ইতিহাস।",
                "২) ধ্বনি পরিবর্তনের মূল কারণসমূহ (শারীরিক ও মানসিক)।",
                "৩) স্বরভক্তি, অপিনিহিতি, অভিশ্রুতির বাস্তব উদাহরণ ও চার্ট।",
                "৪) বাংলা ব্যাকরণে সন্ধির গুরুত্ব ও প্রাত্যহিক প্রয়োগ।",
                "৫) উপসংহার ও ভবিষ্যৎ পর্যবেক্ষণ।"
            ),
            prompt = "ক্লাস ৯ বাংলা ব্যাকরণ প্রজেক্ট: 'ধ্বনি পরিবর্তনের নিয়ম ও বাংলা ভাষার ক্রমবিকাশ' বিষয়ক সম্পূর্ণ প্রজেক্ট ফাইল লিখে দাও।"
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "CBSE ক্লাস ৯ বাংলা প্রজেক্ট গাইড",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "বোর্ডের নির্দেশিকা মেনে নিখুঁত প্রজেক্ট তৈরি করুন ১-ক্লিকে",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(projectGuides, key = { it.title }) { guide ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_guide_${guide.title.hashCode()}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = guide.bookRef,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = guide.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "📋 প্রজেক্টের ধাপসমূহ:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    guide.steps.forEach { step ->
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { onDraftProject(guide.prompt) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("draft_project_${guide.title.hashCode()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("শিক্ষককে দিয়ে সম্পূর্ণ প্রজেক্ট খসড়া তৈরি করান")
                    }
                }
            }
        }
    }
}
