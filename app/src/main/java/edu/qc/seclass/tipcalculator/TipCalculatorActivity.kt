package edu.qc.seclass.tipcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class TipCalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TipCalculatorScreen()
                }
            }
        }
    }
}

/** Per-person tip and total (both rounded to nearest integer) for one tip percentage. */
data class TipResult(val tip: Int, val total: Int)

fun computeTip(check: Double, people: Int, percent: Int): TipResult {
    val tipPerPerson = check * percent / 100.0 / people
    val totalPerPerson = (check + check * percent / 100.0) / people
    return TipResult(tipPerPerson.roundToInt(), totalPerPerson.roundToInt())
}

@Composable
fun TipCalculatorScreen() {
    var checkText by remember { mutableStateOf("") }
    var partyText by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<TipResult>?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showError(msg: String) {
        results = null
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg)
        }
    }

    fun onCompute() {
        if (checkText.isBlank() || partyText.isBlank()) {
            showError("Please enter both the check amount and the party size.")
            return
        }
        val check = checkText.trim().toDoubleOrNull()
        val people = partyText.trim().toIntOrNull()
        if (check == null || check < 0.0 || !check.isFinite()) {
            showError("Check amount must be a non-negative number.")
            return
        }
        if (people == null || people < 1) {
            showError("Party size must be a whole number of at least 1.")
            return
        }
        results = listOf(15, 20, 25).map { computeTip(check, people, it) }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tip Calculator", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = checkText,
                onValueChange = { checkText = it },
                label = { Text("Check Amount") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth().testTag("checkAmountValue")
            )
            OutlinedTextField(
                value = partyText,
                onValueChange = { partyText = it },
                label = { Text("Party Size") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("partySizeValue")
            )
            Button(
                onClick = { onCompute() },
                modifier = Modifier.fillMaxWidth().testTag("buttonCompute")
            ) { Text("Compute") }

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tip %", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Tip / person", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Total / person", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }

            val rows = listOf(
                Triple("15%", "fifteenPercentTipValue", "fifteenPercentTotalValue"),
                Triple("20%", "twentyPercentTipValue", "twentyPercentTotalValue"),
                Triple("25%", "twentyfivePercentTipValue", "twentyfivePercentTotalValue")
            )
            rows.forEachIndexed { i, (label, tipTag, totalTag) ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, modifier = Modifier.weight(1f))
                    Text(
                        results?.get(i)?.tip?.toString() ?: "",
                        modifier = Modifier.weight(1f).testTag(tipTag)
                    )
                    Text(
                        results?.get(i)?.total?.toString() ?: "",
                        modifier = Modifier.weight(1f).testTag(totalTag)
                    )
                }
            }
        }
    }
}