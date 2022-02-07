var tpschart;
var ramchart;
var enginechart;
var cpuchart;

var GLOBAL_CHART_OPTIONS = {}

function initCPUChart() {
    var options = {
        series: [],
        chart: {
            type: 'area',
            id: "chart1",
            background: 'var(--theme-graphs-backcolor)',
            height: "100%"
        },
        theme: {
            mode: theme
        },
        dataLabels: {
            enabled: false
        },
        title: {
            text: '%T%CPU_USAGE%T%',
            align: 'left'
        },
        yaxis: {
            min: 0,
            labels: {
                formatter: function(value) {
                    return value.toFixed(2) + "%";
                }
            }
        }
    };
    options = {...options, ...GLOBAL_CHART_OPTIONS }

    cpuchart = new ApexCharts(document.querySelector("#cpuchart"), options);
    cpuchart.render();
}

function initRAMChart() {
    var options = {
        series: [],
        chart: {
            type: 'area',
            id: "chart2",
            background: 'var(--theme-graphs-backcolor)',
            height: "100%"
        },
        theme: {
            mode: theme
        },
        dataLabels: {
            enabled: false
        },
        title: {
            text: '%T%RAM%T%/%T%STORAGE%T% %T%USAGE%T%',
            align: 'left'
        },
        yaxis: [{
            show: false,
        }, {
            show: false,
        }, {
            show: false,
        }, {
            show: false,
        }]
    };
    options = {...options, ...GLOBAL_CHART_OPTIONS }

    ramchart = new ApexCharts(document.querySelector("#ramchart"), options);
    ramchart.render();
}

function initTPSChart() {
    var options = {
        series: [],
        chart: {
            type: 'area',
            id: "chart3",
            background: 'var(--theme-graphs-backcolor)',
            height: "100%"
        },
        theme: {
            mode: theme,

        },
        dataLabels: {
            enabled: false
        },
        title: {
            text: '%T%TPS_HISTORY%T%',
            align: 'left'
        },
        yaxis: [{
            show: true
        }, {
            show: false,
        }]
    };
    options = {...options, ...GLOBAL_CHART_OPTIONS }

    tpschart = new ApexCharts(document.querySelector("#tpschart"), options);
    tpschart.render();
}

function initEngineChart() {
    var options = {
        series: [],
        chart: {
            type: 'area',
            id: "chart4",
            background: 'var(--theme-graphs-backcolor)',
            height: "100%"
        },
        theme: {
            mode: theme,

        },
        dataLabels: {
            enabled: false
        },
        title: {
            text: '%T%ENGINE_STATS%T%',
            align: 'left'
        },
        yaxis: [{
            show: false
        }, {
            show: false,
        }, {
            show: false,
        }]
    };
    options = {...options, ...GLOBAL_CHART_OPTIONS }

    enginechart = new ApexCharts(document.querySelector("#enginechart"), options);
    enginechart.render();
}



async function initPage() {
    initCPUChart();
    initRAMChart();
    initTPSChart();
    initEngineChart();
    fixDuplicates();
    curr_task = updateData;
}

function fixDuplicates() {
    var ids = [];
    document.querySelectorAll(".apexcharts-canvas").forEach((elem) => {
        var chartid = elem.id.replace("apexcharts", "");
        if (ids.includes(chartid)) {
            elem.remove();
        } else {
            ids.push(chartid);
        }
    });
}

async function updateData() {
    try {

        var data = await getDataFromAPI({ TYPE: "PAGEDATA", PAGE: "GRAPHS" });

        var MIN_STOR = bytesToGB(data[0].USED_SPACE - (data[0].USED_SPACE / 10));
        var MAX_STOR = bytesToGB(data[0].TOTAL_SPACE);

        var RAM_GRAPH = {
            options: {
                yaxis: [{
                    min: 0,
                    max: data[0].MEMORY_MAX,
                    show: false,
                    labels: {
                        formatter: function(value) {
                            return value + " MB";
                        }
                    },
                }, {
                    min: 0,
                    max: data[0].MEMORY_MAX,
                    show: false,
                    labels: {
                        formatter: function(value) {
                            return value + " MB";
                        }
                    },
                }, {
                    show: false,
                    labels: {
                        formatter: function(value) {
                            return value + " GB";
                        }
                    },
                    min: MIN_STOR,
                    max: MAX_STOR,
                }, {
                    show: false,
                    labels: {
                        formatter: function(value) {
                            return value + " GB";
                        }
                    },
                    min: MIN_STOR,
                    max: MAX_STOR,
                }],
            },
            series: [{
                name: "%T%RAM_ALLOCATED%T%",
                data: []
            }, {
                name: "%T%RAM_USED%T%",
                data: []
            }, {
                name: "%T%STORAGE_TOTAL%T%",
                data: []
            }, {
                name: "%T%STORAGE_USED%T%",
                data: []
            }]
        }

        var TPS_GRAPH = {
            series: [{
                name: "%T%TPS_LONG%T%",
                data: []
            }, {
                name: "%T%MSPT_LONG%T%",
                data: []
            }]
        }

        var CPU_GRAPH = {
            series: [{
                name: "%T%CPU_LOAD_HOST%T%",
                data: []
            }, {
                name: "%T%CPU_LOAD_SERVER%T%",
                data: []
            }]
        }

        var ENGINE_GRAPH = {
            series: [{
                    name: "%T%CHUNKS%T%",
                    data: []
                }, {
                    name: "%T%ENTITIES%T%",
                    data: []
                },
                {
                    name: "%T%PLAYERS%T%",
                    data: []
                }
            ]
        }

        data.forEach((elem) => {
            var elem_date = dateFormatter(new Date(elem.DATETIME));
            RAM_GRAPH.series[0].data.push({ x: elem_date, y: elem.MEMORY_ALLOCATED });
            RAM_GRAPH.series[1].data.push({ x: elem_date, y: elem.MEMORY_USED });
            RAM_GRAPH.series[2].data.push({ x: elem_date, y: bytesToGB(elem.TOTAL_SPACE) });
            RAM_GRAPH.series[3].data.push({ x: elem_date, y: bytesToGB(elem.USED_SPACE) });

            TPS_GRAPH.series[0].data.push({ x: elem_date, y: parseFloat(elem.TPS).toFixed(2) });
            if (elem.TPS == 0) elem.TPS = 1;
            TPS_GRAPH.series[1].data.push({ x: elem_date, y: (1000 / parseFloat(elem.TPS)).toFixed(2) });

            CPU_GRAPH.series[0].data.push({ x: elem_date, y: elem.CPU_LOAD_SYSTEM });
            CPU_GRAPH.series[1].data.push({ x: elem_date, y: elem.CPU_LOAD_PROCESS });

            ENGINE_GRAPH.series[0].data.push({ x: elem_date, y: elem.WORLD_CHUNKS });
            ENGINE_GRAPH.series[1].data.push({ x: elem_date, y: elem.WORLD_ENTITIES });
            ENGINE_GRAPH.series[2].data.push({ x: elem_date, y: elem.WORLD_PLAYERS });
        });

        ramchart.updateOptions(RAM_GRAPH.options);
        ramchart.updateSeries(RAM_GRAPH.series);

        tpschart.updateSeries(TPS_GRAPH.series);

        cpuchart.updateSeries(CPU_GRAPH.series);

        enginechart.updateSeries(ENGINE_GRAPH.series);
    } catch (err) {
        console.log(err);
    }

}

function dateFormatter(value) {
    try {
        var date = new Date(value);
        return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    } catch (err) {
        return "ERR";
    }

}

function bytesToGB(bytes) {
    return parseFloat((bytes / (1000 * 1000 * 1000)).toFixed(2));
}