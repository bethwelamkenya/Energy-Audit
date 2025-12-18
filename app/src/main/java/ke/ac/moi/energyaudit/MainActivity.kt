package ke.ac.moi.energyaudit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ke.ac.moi.energyaudit.data.Screen
import ke.ac.moi.energyaudit.ui.screens.AnalyticsScreen
import ke.ac.moi.energyaudit.ui.screens.DashboardScreen
import ke.ac.moi.energyaudit.ui.screens.IconBox
import ke.ac.moi.energyaudit.ui.screens.ReportsScreen
import ke.ac.moi.energyaudit.ui.theme.EnergyAuditTheme
import ke.ac.moi.energyaudit.ui.viewmodel.ChartViewModel
import ke.ac.moi.energyaudit.ui.viewmodel.ChartViewModelFactory
import ke.ac.moi.energyaudit.ui.viewmodel.EnergyViewModel
import ke.ac.moi.energyaudit.ui.viewmodel.EnergyViewModelFactory
import java.util.Locale.getDefault
import kotlin.getValue

class MainActivity : ComponentActivity() {
    // Use the factory to create the ViewModel instance
    private val viewModel: EnergyViewModel by viewModels {
        EnergyViewModelFactory((application as EnergyAuditApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val chartModel: ChartViewModel by viewModels {
                ChartViewModelFactory((application as EnergyAuditApplication).repository)
            }
            EnergyAuditTheme {
                EnergyAuditApp(
                    energyViewModel = viewModel,
                    chartViewModel = chartModel
                )
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    DashboardScreen(
//                        modifier = Modifier.padding(innerPadding),
//                        viewModel = viewModel
//                    )
//                }
            }
        }
    }
}

@Composable
fun EnergyAuditApp(
    energyViewModel: EnergyViewModel,
    chartViewModel: ChartViewModel
) {
    val navController = rememberNavController()
    val currentRoute = navController
        .currentBackStackEntryAsState().value?.destination?.route
    val selected = Screen.screens.find { currentRoute?.startsWith(it.route) == true } ?: Screen.Dashboard

    Scaffold(
        topBar = {
            CardTopBar(
                title = selected.title,
                homePage = selected == Screen.Dashboard,
                onBack = { navController.popBackStack() },
                actions = {}
            )
        },
        bottomBar = { BottomNavBar(navController) }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            EnergyAuditNavGraph(
                navController = navController,
                energyViewModel = energyViewModel,
                chartViewModel = chartViewModel
            )
        }
    }
}

@Composable
fun EnergyAuditNavGraph(
    navController: NavHostController,
    energyViewModel: EnergyViewModel,
    chartViewModel: ChartViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                viewModel = energyViewModel,
                onMeterClick = { navController.navigate("${Screen.Analytics.route}/${it.meterId}") })
        }

        composable("${Screen.Analytics.route}/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            // Example: chart for first meter
            AnalyticsScreen(
                meterId = id,
                energyViewModel = energyViewModel,
                chartViewModel = chartViewModel
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                energyViewModel = energyViewModel,
                chartViewModel = chartViewModel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardTopBar(
    modifier: Modifier = Modifier,
    title: String = "Energy Audit",
    homePage: Boolean = false,
    onBack: () -> Unit,
    actions: @Composable (RowScope.() -> Unit),
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            // 👇 VERY important: respect status bar
            .statusBarsPadding()
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(10.dp)
    ) {
        TopAppBar(
//            CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                if (homePage) {
                    Image(
                        modifier = Modifier.height(60.dp).padding(10.dp),
                        // TODO
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                } else {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.padding(8.dp)
                ) { actions() }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
//                    titleContentColor = MaterialTheme.colorScheme.surface,
//                    navigationIconContentColor = MaterialTheme.colorScheme.surface,
//                    actionIconContentColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val items = listOf(
        Screen.Dashboard,
        Screen.Analytics,
        Screen.Reports
    )

    NavigationBar {
        val currentRoute = navController
            .currentBackStackEntryAsState().value?.destination?.route

        items.forEach { screen ->
            val selected = currentRoute?.startsWith(screen.route) ?: false
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (screen == Screen.Analytics) {
                        navController.navigate("${screen.route}/}") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    } else {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                label = {
                    Text(screen.route.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(
                            getDefault()
                        ) else it.toString()
                    })
                },
                icon = {
                    if (selected){
                        IconBox(
                            icon = screen.icon,
                            size = 50,
                        )
                    } else {
                        Icon(screen.icon, contentDescription = screen.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EnergyAuditTheme {
        Greeting("Android")
    }
}