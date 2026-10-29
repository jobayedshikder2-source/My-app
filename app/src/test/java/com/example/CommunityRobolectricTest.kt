package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Message
import com.example.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CommunityRobolectricTest {

  @Test
  fun `verify app name resource`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Community Hub", appName)
  }

  @Test
  fun `verify user permission rules`() {
    val pendingUser = User(
      id = "u_pending",
      name = "Kamrul",
      status = User.STATUS_PENDING,
      role = User.ROLE_MEMBER
    )
    assertTrue(pendingUser.isPending)
    assertFalse(pendingUser.isApproved)
    assertFalse(pendingUser.canPostInImportantGroup)

    val adminUser = User(
      id = "u_admin",
      name = "Admin",
      status = User.STATUS_APPROVED,
      role = User.ROLE_ADMIN
    )
    assertTrue(adminUser.isApproved)
    assertTrue(adminUser.canPostInImportantGroup)
  }

  @Test
  fun `verify message group structure`() {
    val message = Message(
      id = "m_test",
      groupType = Message.GROUP_IMPORTANT,
      senderId = "u_admin",
      senderName = "Admin",
      text = "Official announcement"
    )
    assertEquals(Message.GROUP_IMPORTANT, message.groupType)
    assertEquals("Official announcement", message.text)
  }
}
