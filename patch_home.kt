    var showCheckingSheet by remember { mutableStateOf(false) }
    var checkingInputText by remember(settings.checkingBalance) { mutableStateOf(settings.checkingBalance.formatTwoDecimals()) }
