package com.example.data.remote

import com.example.data.local.AdminCredentialsEntity
import com.example.data.local.PantryItemEntity
import com.example.data.local.ToBuyItemEntity
import com.example.data.local.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WebhookManager {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun syncWithGoogleSheets(
        webhookUrl: String,
        pantryItems: List<PantryItemEntity>,
        toBuyItems: List<ToBuyItemEntity>,
        users: List<UserEntity>,
        adminCreds: AdminCredentialsEntity?,
        initiator: String,
        actionType: String = "FULL_SYNC"
    ): Result<String> = withContext(Dispatchers.IO) {
        if (webhookUrl.isBlank() || !webhookUrl.startsWith("http")) {
            return@withContext Result.failure(IllegalArgumentException("Please configure a valid Google Sheets App Script Webhook URL."))
        }

        try {
            val rootJson = JSONObject().apply {
                put("action", actionType)
                put("syncedAt", System.currentTimeMillis())
                put("syncedBy", initiator)

                // 1. Admin Credentials saved in Google Sheet
                if (adminCreds != null) {
                    val adminObj = JSONObject().apply {
                        put("adminUserId", adminCreds.adminUserId)
                        put("adminPassword", adminCreds.adminPasswordHash)
                        put("lastSaved", System.currentTimeMillis())
                    }
                    put("adminCredentials", adminObj)
                }

                // 2. All Registered User Accounts
                val userArray = JSONArray()
                for (u in users) {
                    val uObj = JSONObject().apply {
                        put("id", u.id)
                        put("name", u.name)
                        put("identifier", u.identifier)
                        put("role", u.role)
                        put("status", u.status)
                        put("companion", u.companionIdentifier ?: "None")
                        put("companionRole", u.companionRole)
                        put("currency", u.currencySymbol)
                        put("backupFrequency", u.backupFrequency)
                        put("createdAt", u.createdAt)
                    }
                    userArray.put(uObj)
                }
                put("userAccounts", userArray)

                // 3. Pantry Items (including Rate and Medicine)
                val pantryArray = JSONArray()
                for (item in pantryItems) {
                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("nameEn", item.nameEn)
                        put("nameBn", item.nameBn)
                        put("category", item.category)
                        put("quantity", item.quantity)
                        put("unit", item.unit)
                        put("rate", item.rate)
                        put("price", item.price)
                        put("isLowStock", item.isLowStock)
                        put("isFinished", item.isFinished)
                        put("isPrivate", item.isPrivate)
                    }
                    pantryArray.put(obj)
                }
                put("pantryItems", pantryArray)

                // 4. To Buy Items
                val toBuyArray = JSONArray()
                for (item in toBuyItems) {
                    val obj = JSONObject().apply {
                        put("id", item.id)
                        put("nameEn", item.nameEn)
                        put("nameBn", item.nameBn)
                        put("category", item.category)
                        put("quantity", item.quantity)
                        put("unit", item.unit)
                        put("rate", item.rate)
                        put("estimatedPrice", item.estimatedPrice)
                        put("isBought", item.isBought)
                        put("isPrivate", item.isPrivate)
                    }
                    toBuyArray.put(obj)
                }
                put("toBuyItems", toBuyArray)
            }

            val requestBody = rootJson.toString().toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(webhookUrl)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success("Sync successful! Response: ${bodyString.take(120)}")
            } else {
                Result.failure(Exception("HTTP Error ${response.code}: $bodyString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    const val GOOGLE_APPS_SCRIPT_SAMPLE = """
// --- PASTE INTO GOOGLE SHEETS -> Extensions -> Apps Script ---
function doPost(e) {
  try {
    var data = JSON.parse(e.postData.contents);
    var ss = SpreadsheetApp.getActiveSpreadsheet();

    // 1. Admin Credentials Sheet
    if (data.adminCredentials) {
      var sheetAdmin = ss.getSheetByName("Admin_Config") || ss.insertSheet("Admin_Config");
      if (sheetAdmin.getLastRow() === 0) {
        sheetAdmin.appendRow(["Admin User ID", "Admin Password", "Last Synced Time"]);
      }
      sheetAdmin.getRange(2, 1, 1, 3).setValues([
        [data.adminCredentials.adminUserId, data.adminCredentials.adminPassword, new Date()]
      ]);
    }

    // 2. User Accounts Sheet
    if (data.userAccounts && data.userAccounts.length > 0) {
      var sheetUsers = ss.getSheetByName("Users") || ss.insertSheet("Users");
      if (sheetUsers.getLastRow() === 0) {
        sheetUsers.appendRow(["User ID", "Name", "Identifier", "Role", "Status", "Companion", "Currency", "Backup Schedule"]);
      }
      var lastUserRow = sheetUsers.getLastRow();
      if (lastUserRow > 1) {
        sheetUsers.getRange(2, 1, lastUserRow - 1, 8).clearContent();
      }
      var userRows = data.userAccounts.map(function(u) {
        return [u.id, u.name, u.identifier, u.role, u.status, u.companion, u.currency, u.backupFrequency];
      });
      sheetUsers.getRange(2, 1, userRows.length, 8).setValues(userRows);
    }

    // 3. Pantry Inventory (with Rates & Medicine)
    if (data.pantryItems) {
      var sheetPantry = ss.getSheetByName("Pantry") || ss.insertSheet("Pantry");
      if (sheetPantry.getLastRow() === 0) {
        sheetPantry.appendRow(["Item (EN)", "Item (BN)", "Category", "Quantity", "Unit", "Rate", "Price", "Low Stock?", "Private?"]);
      }
      var lastPantryRow = sheetPantry.getLastRow();
      if (lastPantryRow > 1) {
        sheetPantry.getRange(2, 1, lastPantryRow - 1, 9).clearContent();
      }
      if (data.pantryItems.length > 0) {
        var rows = data.pantryItems.map(function(item) {
          return [item.nameEn, item.nameBn, item.category, item.quantity, item.unit, item.rate, item.price, item.isLowStock, item.isPrivate];
        });
        sheetPantry.getRange(2, 1, rows.length, 9).setValues(rows);
      }
    }

    // 4. To-Buy Market List
    if (data.toBuyItems) {
      var sheetBuy = ss.getSheetByName("ToBuy") || ss.insertSheet("ToBuy");
      if (sheetBuy.getLastRow() === 0) {
        sheetBuy.appendRow(["Item (EN)", "Item (BN)", "Category", "Quantity", "Unit", "Rate", "Est Price", "Bought?", "Private?"]);
      }
      var lastBuyRow = sheetBuy.getLastRow();
      if (lastBuyRow > 1) {
        sheetBuy.getRange(2, 1, lastBuyRow - 1, 9).clearContent();
      }
      if (data.toBuyItems.length > 0) {
        var buyRows = data.toBuyItems.map(function(item) {
          return [item.nameEn, item.nameBn, item.category, item.quantity, item.unit, item.rate, item.estimatedPrice, item.isBought, item.isPrivate];
        });
        sheetBuy.getRange(2, 1, buyRows.length, 9).setValues(buyRows);
      }
    }

    return ContentService.createTextOutput(JSON.stringify({status: "success", action: data.action, timestamp: new Date()}))
      .setMimeType(ContentService.MimeType.JSON);
  } catch (err) {
    return ContentService.createTextOutput(JSON.stringify({status: "error", error: err.message}))
      .setMimeType(ContentService.MimeType.JSON);
  }
}
"""
}
