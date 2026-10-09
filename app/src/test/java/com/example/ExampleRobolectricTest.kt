package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ToBuyItemEntity
import com.example.util.PdfGenerator
import com.example.util.VoiceCommandResult
import com.example.util.VoiceParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kitchen Pantry", appName)
  }

  @Test
  fun `test bengali voice command alu shesh`() {
    val result = VoiceParser.parse("আলু শেষ")
    assertTrue(result is VoiceCommandResult.MarkFinished)
    val finishResult = result as VoiceCommandResult.MarkFinished
    assertEquals("Potato", finishResult.itemEn)
    assertEquals("আলু", finishResult.itemBn)
  }

  @Test
  fun `test english voice command potato finish`() {
    val result = VoiceParser.parse("potato finish")
    assertTrue(result is VoiceCommandResult.MarkFinished)
    val finishResult = result as VoiceCommandResult.MarkFinished
    assertEquals("Potato", finishResult.itemEn)
  }

  @Test
  fun `test medicine voice command napa finish`() {
    val result = VoiceParser.parse("নাপা শেষ")
    assertTrue(result is VoiceCommandResult.MarkFinished)
    val finishResult = result as VoiceCommandResult.MarkFinished
    assertEquals("Paracetamol", finishResult.itemEn)
    assertEquals("MEDICINE", finishResult.category)
  }

  @Test
  fun `test pdf generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sampleItems = listOf(
      ToBuyItemEntity(
        id = 1,
        nameEn = "Potato",
        nameBn = "আলু",
        category = "VEGETABLES",
        quantity = 5.0,
        unit = "kg",
        rate = 30.0,
        estimatedPrice = 150.0
      ),
      ToBuyItemEntity(
        id = 2,
        nameEn = "Paracetamol",
        nameBn = "প্যারাসিটামল",
        category = "MEDICINE",
        quantity = 2.0,
        unit = "strip",
        rate = 20.0,
        estimatedPrice = 40.0
      )
    )
    val pdf = PdfGenerator.generateShoppingListPdf(context, sampleItems, "Tariqul & Family", "৳")
    assertNotNull(pdf)
    assertTrue(pdf!!.exists())
    assertTrue(pdf.length() > 0)
  }
}
