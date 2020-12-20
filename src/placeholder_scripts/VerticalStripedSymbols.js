function placeholder() {
	return "{VerticalStripedSymbols}";
}

function parse(customname, mobtype, health, maxhealth) {
	var healthyChar = "&a|";
	var halfChar = "&e|";
	var emptyChar = "&7|";
	var heartScale = 20;

	var percentage = (health / maxhealth) * 100;
	var healthpercentagescaled = percentage / 100.0 * heartScale;
	var fullhearts = Math.floor(healthpercentagescaled);

	var text = "";
	var i;

	for (i = 0; i < fullhearts; i++) {
		text += healthyChar;
	}
	if (fullhearts < heartScale) {
		var leftover = healthpercentagescaled - fullhearts;
		if (leftover > 0.67) {
			text += healthyChar;
		} else if (leftover > 0.33) {
			text += halfChar;
		} else {
			text += emptyChar;
		}
		for (i = fullhearts + 1; i < heartScale; i++) {
			text += emptyChar;
		}
	}
	return text;
}