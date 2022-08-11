let canvas = null;
let currencyDates = [];
let currencyValues = [];

function CurrencyExchange() {
    let currencyValue = 0.0;
    const val = document.querySelector('input').value;
    axios
    .get('http://localhost:8080/currency/latest')
    .then(res => {
        if(res.status == 200) {
            currencyValue = res.data.rates['USD'];
            document.getElementById("usd").value = val * currencyValue;
        }
    })
    .catch(error => {
        console.error(error);
    });
}

function getLastWeekDate() {
    const now = new Date();

    return new Date(now.getFullYear(), now.getMonth(), now.getDate() - 7);
}

function getLastMonthDate() {
    const now = new Date();

    return new Date(now.getFullYear(), now.getMonth() - 1, now.getDate());
}

function getLastYearsDate(years) {
    const now = new Date();

    return new Date(now.getFullYear() - years, now.getMonth(), now.getDate());
}

function parseDateAsString(date) {
    var dd = String(date.getDate()).padStart(2, '0');
    var mm = String(date.getMonth() + 1).padStart(2, '0'); //January is 0!
    var yyyy = date.getFullYear();

    return yyyy+'-'+mm+'-'+dd;
}

async function makeRequest(period) {
    currencyDates = [];
    currencyValues = [];

    var startDate = null
    var endDate = parseDateAsString(new Date()); // reference will always be today
    
    if(period == '1W') {
        startDate = parseDateAsString(getLastWeekDate());
    }
    else if(period == '1M') {
        startDate = parseDateAsString(getLastMonthDate());
    }
    else if(period == '1Y') {
        startDate = parseDateAsString(getLastYearsDate(1));
    }
    else if(period == '5Y') {
        startDate = parseDateAsString(getLastYearsDate(5));
    }

    await axios
    .get('http://localhost:8080/currency/interval?startDate=' + startDate + '&endDate=' + endDate)
    .then(res => {
        if(res.status == 200) {
            for(const x of res.data) {
                currencyDates.push(x['date'].split('T')[0]);
                currencyValues.push(x.rates['USD'])
            }
        }
    })
    .catch(error => {
        console.error(error);
    });
}

async function redrawChart(period) {
    await makeRequest(period);

    if(canvas != null) {
        canvas.destroy();
    }

    var ctx = document.getElementById('chart').getContext('2d');
    canvas = new Chart(ctx, {
        // The type of chart we want to create
        type: 'line',

        // The data for our dataset
        data: {
            labels: currencyDates,
            datasets: [{
                label: 'BRL/USD',
                backgroundColor: 'rgb(255, 99, 132)',
                borderColor: 'rgb(255, 99, 132)',
                data: currencyValues
            }]
        },

        // Configuration options go here
        options: {
            scales:{
                x: {
                    display: false
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            },
            tooltips: {
                callbacks: {
                label: function(tooltipItem) {
                        return tooltipItem.yLabel;
                }
                }
            }
        }
    });
}

if(canvas != null) {
    canvas.destroy();
}

var ctx = document.getElementById('chart').getContext('2d');
canvas = new Chart(ctx, {
    // The type of chart we want to create
    type: 'line',

    // The data for our dataset
    data: {
        labels: [],
        datasets: [{
            label: 'BRL/USD',
            backgroundColor: 'rgb(255, 99, 132)',
            borderColor: 'rgb(255, 99, 132)',
            data: []
        }]
    },

    // Configuration options go here
    options: {
        scales:{
            x: {
                display: false
            }
        },
        plugins: {
            legend: {
                display: false
            }
        },
        tooltips: {
            callbacks: {
            label: function(tooltipItem) {
                    return tooltipItem.yLabel;
            }
            }
        }
    }
});