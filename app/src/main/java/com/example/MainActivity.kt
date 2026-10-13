package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.AuthConfig
import com.example.auth.AuthManager
import com.example.firebase.ModuleProgressManager
import com.example.market.PortfolioManager
import com.example.ui.AdminDataPanelScreen
import com.example.ui.admin.AdminPanelScreen
import com.example.ui.AiSecurityHubScreen
import com.example.ui.CodingAcademyScreen
import com.example.ui.DetectionScreen
import com.example.ui.GameSecNavTab
import com.example.ui.HomeScreen
import com.example.ui.InvestigationScreen
import com.example.ui.MarketScreen
import com.example.ui.ModulesScreen
import com.example.ui.PortfolioScreen
import com.example.ui.ProfileScreen
import com.example.ui.QuizScreen
import com.example.ui.SchoolTasksScreen
import com.example.ui.SecurityLabScreen
import com.example.ui.TerminalScreen
import com.example.ui.ToolsHubScreen
import com.example.ui.components.CyberRadarScanner
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GameSecLabTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    AuthManager.init(this)
    PortfolioManager.init(this)
    ModuleProgressManager.init(this)

    setContent {
      GameSecLabTheme {
        GameSecApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSecApp() {
  val context = LocalContext.current
  remember {
    AuthManager.init(context)
    PortfolioManager.init(context)
    ModuleProgressManager.init(context)
  }

  val session by AuthManager.sessionState.collectAsState()
  val isAdmin = AuthManager.isAdmin() && session.email.trim().equals(AuthConfig.ADMIN_EMAIL, ignoreCase = true)

  var selectedTab by rememberSaveable { mutableStateOf(GameSecNavTab.TUGAS_SEKOLAH) }
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  // Handle system back button:
  // 1. If drawer is open -> close it
  // 2. If on a tab other than TUGAS_SEKOLAH -> return to TUGAS_SEKOLAH
  BackHandler(enabled = drawerState.isOpen || selectedTab != GameSecNavTab.TUGAS_SEKOLAH) {
    if (drawerState.isOpen) {
      scope.launch { drawerState.close() }
    } else {
      selectedTab = GameSecNavTab.TUGAS_SEKOLAH
    }
  }

  // All visible drawer tabs (ADMIN_PANEL is open and accessible without restrictions)
  val availableTabs = remember {
    GameSecNavTab.entries.filter { tab -> tab.isDrawerVisible }
  }

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    val isWideScreen = maxWidth >= 600.dp

    if (isWideScreen) {
      // Tablet / Landscape NavigationRail layout
      Row(modifier = Modifier.fillMaxSize()) {
        NavigationRail(
          containerColor = DarkSurface,
          contentColor = TextPrimary,
          modifier = Modifier
            .fillMaxHeight()
            .testTag("app_navigation_rail")
        ) {
          availableTabs.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationRailItem(
              selected = isSelected,
              onClick = { selectedTab = tab },
              icon = {
                Icon(
                  imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                  contentDescription = tab.label
                )
              },
              label = {
                Text(
                  text = tab.label,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = NavigationRailItemDefaults.colors(
                selectedIconColor = DarkBackground,
                selectedTextColor = AccentGreen,
                indicatorColor = AccentGreen,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
              ),
              modifier = Modifier.testTag(tab.testTag)
            )
          }
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .weight(1f)
        ) {
          MainContentDispatcher(
            selectedTab = selectedTab,
            onNavigateToTab = { selectedTab = it },
            modifier = Modifier.fillMaxSize()
          )
        }
      }
    } else {
      // Mobile Layout with TopAppBar + Navigation Drawer + Bottom Navigation Bar
      ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
          ModalDrawerSheet(
            drawerContainerColor = DarkSurface,
            drawerContentColor = TextPrimary,
            modifier = Modifier
              .width(310.dp)
              .testTag("app_navigation_drawer")
          ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                ) {
                  // Drawer Brand Header
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                  ) {
                    CyberRadarScanner(modifier = Modifier.size(28.dp))
                    Column {
                      Text(
                        text = "FAHMZY LAB",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen
                      )
                      Text(
                        text = "Keamanan Game & Financial Intel",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = TextSecondary
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(14.dp))

                  // User Identity Badge in Drawer
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceVariant,
                    border = BorderStroke(
                      1.dp,
                      if (isAdmin) AccentGreen else if (session.isLoggedIn) AccentBlue else DarkCardBorder
                    ),
                    modifier = Modifier
                      .fillMaxWidth()
                      .clickable {
                        selectedTab = GameSecNavTab.PROFIL
                        scope.launch { drawerState.close() }
                      }
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                      Surface(
                        shape = CircleShape,
                        color = if (isAdmin) AccentGreen.copy(alpha = 0.2f) else DarkSurface,
                        modifier = Modifier.size(32.dp)
                      ) {
                        Box(contentAlignment = Alignment.Center) {
                          Icon(
                            imageVector = if (isAdmin) Icons.Default.Security else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isAdmin) AccentGreen else TextMuted,
                            modifier = Modifier.size(16.dp)
                          )
                        }
                      }

                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = if (session.isLoggedIn) session.displayName.ifBlank { "Pengguna Google" } else "Tamu Anonim",
                          style = MaterialTheme.typography.bodyMedium,
                          fontWeight = FontWeight.Bold,
                          color = TextPrimary,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                        Text(
                          text = if (session.isLoggedIn) session.email else "Belum tertaut akun",
                          fontFamily = FontFamily.Monospace,
                          fontSize = 10.sp,
                          color = if (isAdmin) AccentGreen else TextMuted,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }

                      Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = if (isAdmin) AccentGreen else if (session.isLoggedIn) AccentBlue.copy(alpha = 0.2f) else DarkSurface
                      ) {
                        Text(
                          text = if (isAdmin) "ADMIN" else if (session.isLoggedIn) "TERTAUT" else "LOKAL",
                          fontFamily = FontFamily.Monospace,
                          fontSize = 8.5.sp,
                          fontWeight = FontWeight.Bold,
                          color = if (isAdmin) DarkBackground else if (session.isLoggedIn) AccentBlue else TextMuted,
                          modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))
                  HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)
                  Spacer(modifier = Modifier.height(10.dp))

                  // Drawer Navigation Items List (Scrollable)
                  Column(
                    modifier = Modifier
                      .weight(1f)
                      .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    availableTabs.forEach { tab ->
                      val isSelected = selectedTab == tab
                      val isSpecialAdmin = tab == GameSecNavTab.ADMIN_PANEL

                      NavigationDrawerItem(
                        icon = {
                          Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = if (isSelected) {
                              DarkBackground
                            } else if (isSpecialAdmin) {
                              AccentGreen
                            } else {
                              TextMuted
                            }
                          )
                        },
                        label = {
                          Column {
                            Text(
                              text = tab.label,
                              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                              fontSize = 13.5.sp,
                              color = if (isSelected) DarkBackground else if (isSpecialAdmin) AccentGreen else TextPrimary
                            )
                            if (tab.subtitle.isNotEmpty()) {
                              Text(
                                text = tab.subtitle,
                                fontSize = 10.sp,
                                color = if (isSelected) DarkBackground.copy(alpha = 0.75f) else TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                              )
                            }
                          }
                        },
                        selected = isSelected,
                        onClick = {
                          selectedTab = tab
                          scope.launch { drawerState.close() }
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                          selectedContainerColor = AccentGreen,
                          unselectedContainerColor = if (isSpecialAdmin) AccentGreen.copy(alpha = 0.1f) else DarkSurface
                        ),
                        modifier = Modifier
                          .fillMaxWidth()
                          .testTag(tab.testTag)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(8.dp))
                  HorizontalDivider(thickness = 0.5.dp, color = DarkCardBorder)
                  Spacer(modifier = Modifier.height(8.dp))

                  // Drawer Footer Info
                  Text(
                    text = "FAHMZY LAB v1.0 • Offline Ready",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    color = TextMuted,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                  )
                }
              }
            }
          ) {
          Scaffold(
              modifier = Modifier.fillMaxSize(),
              containerColor = DarkBackground,
              topBar = {
                Surface(
                  color = DarkSurface,
                  border = BorderStroke(1.dp, DarkCardBorder),
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("app_top_bar")
                ) {
                  CenterAlignedTopAppBar(
                    title = {
                      Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                      ) {
                        Text(
                          text = "FAHMZY LAB",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = TextPrimary
                        )
                        Surface(
                          shape = RoundedCornerShape(4.dp),
                          color = DarkSurfaceVariant
                        ) {
                          Text(
                            text = selectedTab.label,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                          )
                        }
                      }
                    },
                    navigationIcon = {
                      Box(
                        modifier = Modifier.padding(start = 12.dp),
                        contentAlignment = Alignment.Center
                      ) {
                        CyberRadarScanner(modifier = Modifier.size(24.dp))
                      }
                    },
                    actions = {
                      IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.testTag("btn_menu_drawer")
                      ) {
                        Icon(
                          imageVector = Icons.Default.Menu,
                          contentDescription = "Buka Menu Utama",
                          tint = AccentGreen
                        )
                      }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                      containerColor = DarkSurface
                    )
                  )
                }
              },
              bottomBar = {
                NavigationBar(
                  containerColor = DarkSurface,
                  contentColor = TextPrimary,
                  modifier = Modifier.testTag("app_bottom_bar")
                ) {
                  val primaryTabs = listOf(
                    GameSecNavTab.TUGAS_SEKOLAH,
                    GameSecNavTab.MARKET,
                    GameSecNavTab.BERANDA,
                    GameSecNavTab.PORTFOLIO,
                    GameSecNavTab.ADMIN_PANEL
                  )
                  primaryTabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                      selected = isSelected,
                      onClick = { selectedTab = tab },
                      icon = {
                        Icon(
                          imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                          contentDescription = tab.label
                        )
                      },
                      label = {
                        Text(
                          text = tab.label,
                          fontSize = 10.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                      },
                      colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = AccentGreen,
                        indicatorColor = AccentGreen,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                      ),
                      modifier = Modifier.testTag("bottom_nav_${tab.name.lowercase()}")
                    )
                  }
                }
              }
            ) { innerPadding ->
              MainContentDispatcher(
                selectedTab = selectedTab,
                onNavigateToTab = { selectedTab = it },
                modifier = Modifier
                  .fillMaxSize()
                  .padding(innerPadding)
              )
            }
          }
    }
  }
}

@Composable
fun MainContentDispatcher(
  selectedTab: GameSecNavTab,
  onNavigateToTab: (GameSecNavTab) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier) {
    when (selectedTab) {
      GameSecNavTab.TUGAS_SEKOLAH -> SchoolTasksScreen()
      GameSecNavTab.BERANDA -> HomeScreen(onNavigateToTab = onNavigateToTab)
      GameSecNavTab.MODUL -> ModulesScreen()
      GameSecNavTab.TERMINAL -> TerminalScreen()
      GameSecNavTab.DETEKSI -> DetectionScreen()
      GameSecNavTab.KUIS -> QuizScreen()
      GameSecNavTab.MARKET -> MarketScreen(onNavigateToPortfolio = { onNavigateToTab(GameSecNavTab.PORTFOLIO) })
      GameSecNavTab.PORTFOLIO -> PortfolioScreen(onNavigateToProfile = { onNavigateToTab(GameSecNavTab.PROFIL) })
      GameSecNavTab.TOOLS_HUB -> ToolsHubScreen()
      GameSecNavTab.PROFIL -> ProfileScreen()
      GameSecNavTab.ADMIN_PANEL -> AdminPanelScreen(onNavigateToProfile = { onNavigateToTab(GameSecNavTab.PROFIL) })
      GameSecNavTab.AI_HUB -> AiSecurityHubScreen()
      GameSecNavTab.SECURITY_LAB -> SecurityLabScreen()
      GameSecNavTab.INVESTIGASI -> InvestigationScreen()
      GameSecNavTab.CODING -> CodingAcademyScreen()
    }
  }
}
