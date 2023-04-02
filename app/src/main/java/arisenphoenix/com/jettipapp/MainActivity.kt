package arisenphoenix.com.jettipapp
//import android.content.ContentValues.TAG
//import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import arisenphoenix.com.jettipapp.components.InputField
import arisenphoenix.com.jettipapp.ui.theme.JetTipAppTheme
import arisenphoenix.com.jettipapp.widgets.RoundIconButton
import kotlin.math.round

@ExperimentalComposeUiApi
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                App {
                    AppContents()
                }
            }
        }
    }


fun typeChecker(obj: Any): String{
    when (obj) {
        is String -> {
            return "String"
        }
        is Int -> {
            return "Int"
        }
        is Float -> {
            return "Float"
        }
        is Double -> {
            return "Double"
        }
        is Long -> {
            return "Long"
        }
    }
    return "Not A Known Data Type"
}


@Composable
fun App(content: @Composable () -> Unit){
    JetTipAppTheme {
        Surface(color = MaterialTheme.colors.background) {
            content()
        }
    }
}

@androidx.annotation.OptIn
@ExperimentalComposeUiApi
@Composable
fun AppContents() {
    val totalPerPerson = remember {
        mutableStateOf(0.0)
    }

    Column{
        TopHeader(total = totalPerPerson.value)
        MainContent(pullUpFun = {newTotalPerPerson -> totalPerPerson.value = newTotalPerPerson})
    }
}


@Composable
fun TopHeader(total: Double = 0.00){
    Surface(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)
        .clip(shape = CircleShape.copy(all = CornerSize(12.dp))),
        color = Color(0xFFC788E9)

    ){
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center){
            val fixedTotal = "%.2f".format((total))
            Text(text = "Total Per Person", style=MaterialTheme.typography.h5)
            Text("$$fixedTotal", style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.ExtraBold
                )
        }
    }
}


@ExperimentalComposeUiApi
@Composable
fun MainContent(pullUpFun: (Double) -> Unit){
    BillForm(pullUpFun=pullUpFun)
}

@ExperimentalComposeUiApi
@Composable
fun BillForm(modifier: Modifier = Modifier,
             pullUpFun: (Double) -> Unit,
             onValChange: (String) -> Unit = {}
){
    val keyboardController = LocalSoftwareKeyboardController.current
    val totalBillState = remember{
        mutableStateOf("")
    }
    val isTotalBillStateValid = remember(totalBillState.value) {
        totalBillState.value.trim().isNotEmpty() && totalBillState.value != "." && totalBillState.value.toDouble() != 0.0
    }
    val splitNumber = remember {
        mutableStateOf(1)
    }
    val sliderValue = remember {
        mutableStateOf(0F)
    }
    val sliderStringNum = round(String.format("%.2f", sliderValue.value * 100).toFloat())

    Surface(modifier = Modifier
        .padding(2.dp)
        .fillMaxWidth(),
        shape= RoundedCornerShape(corner = CornerSize(8.dp)),
        border = BorderStroke(width=2.dp, color=Color.LightGray)
    ){
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            InputField(
                modifier = Modifier.fillMaxWidth(),
                valueState = totalBillState,
                labelId = "Enter Bill",
                enabled = true,
                isSingleLine = true,
                keyboardType = KeyboardType.Number,
                onAction=KeyboardActions{
                    if (!isTotalBillStateValid) return@KeyboardActions
                    onValChange(totalBillState.value.trim())
                    keyboardController?.hide()
                })
//            if (isTotalBillStateValid){
            Split(splitNumber = splitNumber.value, add={splitNumber.value += 1}, subtract = {if (splitNumber.value > 1) splitNumber.value -= 1})
            Tip(sliderStringNum.toDouble(), if (isTotalBillStateValid) totalBillState.value.toDouble() else  0.00 )
            TipSlider(sliderStringNum = sliderStringNum, sliderValue = sliderValue.value){
                newVal -> sliderValue.value = newVal
            }



        if (isTotalBillStateValid) {
            pullUpFun(totalBillState.value.toDouble()/splitNumber.value)
        } else {
            pullUpFun(0.00)
        }

            if((sliderStringNum > 0) && isTotalBillStateValid){
                val currentTipAmount =
                    (sliderStringNum * totalBillState.value.toFloat()) / 100 / splitNumber.value
                val billPerPerson = totalBillState.value.toFloat() / splitNumber.value
                val total = (currentTipAmount + billPerPerson).toDouble()
                pullUpFun(total)
            }
        }
    }
}


@Composable
fun TipSlider(sliderStringNum: Float, sliderValue: Float, onChange: (Float) -> Unit){
    Text(text="${sliderStringNum.toInt()}%")
    Slider(modifier=Modifier.width(250.dp), value = sliderValue, onValueChange = onChange)
}


@Composable
fun Split(splitNumber: Int = 1, add: () -> Unit, subtract: () -> Unit){
    //            SPLIT ROW
    Row(modifier = Modifier.padding(3.dp),
        horizontalArrangement = Arrangement.Start){
        Text(text="Split", modifier = Modifier.align(alignment = Alignment.CenterVertically))
        Spacer(modifier = Modifier.width(120.dp))
        Row(modifier = Modifier.padding(horizontal = 3.dp), horizontalArrangement = Arrangement.End){
            RoundIconButton(imageVector = Icons.Default.Remove, onClick = subtract)
            Text(text="$splitNumber", modifier = Modifier.align(Alignment.CenterVertically))
            RoundIconButton(imageVector = Icons.Default.Add, onClick = add)
        }
    }
}

@Composable
fun Tip(tipPercent: Double = 0.00, billAmount: Double = 0.00){
//    , updateTip: (Double) -> Unit
    //            Tip Row
    val tipAmount = (tipPercent/100) * billAmount

    Row(modifier = Modifier
        .padding(horizontal = 3.dp, vertical = 12.dp)
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(text="Tip", modifier = Modifier.padding(start= 30.dp, end=0.dp))
        Spacer(modifier = Modifier.width(160.dp))
        Column(modifier = Modifier.padding(start = 0.dp, end = 70.dp)){
            Text(text="$${round(String.format("%.2f",tipAmount).toFloat())}", modifier = Modifier.align(alignment = Alignment.Start))
        }
//        if (tipAmount > 0){
////        updateTotalBill(formatToTwoDecimals(tipAmount))
//            updateTip(round(tipAmount))
//        }
    }
}


@Preview(showBackground = true)
@ExperimentalComposeUiApi
@Composable
fun DefaultPreview() {
    App {
        AppContents()
    }
}


