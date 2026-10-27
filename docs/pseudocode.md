## withdrawal pseudocode
```
if (client is student) {
	if (today is weekday) {
		fee = 0;
	}
	else {
		fee = 0.1% of withdrawn amount;
	}
}
else {
	if (balance < 1000) {
		fee = 0.3% of withdrawn amount;
	}
	else if (balance >= 1000 and balance < 5000) {
		fee = 0.1% of withdrawn amount;
	}
	else { // balance >= 5000
		fee = 0;
	}
}
```

## deposit pseudocode
```
if (client is student) {
	if (amount > 50) {
		if (balance > 500) {
			fee = 0.5% of deposited amount;
		}
		else {
			fee = 0.25% of deposited amount;
		}
	}
	else if (amount <= 50) {
		if (balance > 5000) {
			fee = 0.5% of deposited amount;
		}
		else {
			fee = 0;
		}
	}
}
else {
	if (amount > 250) {
		if (balance > 2500) {
			fee = 0.8% of deposited amount;
		}
		else {
			fee = 0.4% of deposited amount;
		}
	}
	else {
		if (balance > 10000) {
			fee = 0;
		}
		else {
			fee = 0.1% of deposited amount;
		}
	}
}
```

## transfer pseudocode
```
if (client is student) {
	if (amount < 200) {
		if (fromBalance < 2000) {
			if (toBalance < 1000) {
				fee = 0.1% of transferred amount;
			}
			else {
				fee = 0.05% of transferred amount;
			}
		}
		else { // fromBalance >= 2000
			if (toBalance < 1000) {
				fee = 0.05% of transferred amount;
			}
			else {
				fee = 0.025% of transferred amount;
			}
		}
	}
	else { // amount >= 200
		if (fromBalance < 2000) {
			if (toBalance < 1000) {
				fee = 0.05% of transferred amount;
			}
			else {
				fee = 0.025% of transferred amount;
			}
		}
		else { // fromBalance >= 2000
			if (toBalance < 1000) {
				fee = 0.25% of transferred amount;
			}
			else {
				fee = 0.125% of transferred amount;
			}
		}
	}
}
else {
	if (amount < 100) {
		if (fromBalance < 4000) {
			if (toBalance < 2000) {
				fee = 0.2% of transferred amount;
			}
			else {
				fee = 0.1% of transferred amount;
			}
		}
		else { // fromBalance >= 4000
			if (toBalance < 2000) {
				fee = 1% of transferred amount;
			}
			else {
				fee = 0.5% of transferred amount;
			}
		}
	}
	else { // amount >= 100
		if (fromBalance < 2000) {
			if (toBalance < 1000) {
				fee = 0.2% of transferred amount;
			}
			else {
				fee = 0.1% of transferred amount;
			}
		}
		else { // fromBalance >= 2000
			if (toBalance < 1000) {
				fee = 0.5% of transferred amount;
			}
			else {
				fee = 0.25% of transferred amount;
			}
		}
	}
}
```
