const width = window.innerWidth; // Pencere genişliğinin tamamını kullan
const height = 800;
const margin = { top: 20, right: 120, bottom: 20, left: width / 2 }; // Orta noktadan başlaması için sol margin ayarı

const svgElement = d3.select("#tree-container").append("svg")
    .attr("width", width)
    .attr("height", height)
    .style("background-color", "#f0f0f0"); // Arka plan rengi

const svg = svgElement.append("g")
    .attr("transform", `translate(${margin.left},${margin.top})`); // SVG'nin transformunu ayarla

let treeLayout = d3.tree()
    .size([height - margin.top - margin.bottom, width - margin.left - margin.right])
    .nodeSize([220, 300]); // Her düğüm için daha fazla boşluk

const zoom = d3.zoom()
    .scaleExtent([0.5, 2]) // Zoom seviyesi
    .on("zoom", (event) => {
        svg.attr("transform", event.transform); // Zoom yaparken transformu güncelle
    });

svgElement.call(zoom); // Zoom fonksiyonunu SVG elemanına uygula

document.getElementById('uploadButton').addEventListener('click', () => {
    const file = document.getElementById('fileInput').files[0];
    if (!file) {
        alert('Lütfen bir dosya seçin!');
        return;
    }

    const formData = new FormData();
    formData.append('file', file);

    fetch('/pdf/upload', {
        method: 'POST',
        body: formData,
    })
    .then(response => response.json())
    .then(data => {
        const root = d3.hierarchy(data, d => d.children);
        treeLayout(root); // Ağacı layouta göre düzenle

        const links = treeLayout(root).links();
        const linkPathGenerator = d3.linkVertical()
            .x(d => d.x)
            .y(d => d.y);

        svg.selectAll(".link").remove();
        svg.selectAll(".node").remove();

        svg.selectAll(".link")
            .data(links)
            .enter().append("path")
            .attr("class", "link")
            .attr("d", linkPathGenerator)
            .attr("fill", "none")
            .attr("stroke", "#555")
            .attr("stroke-width", "1.5px");

        const node = svg.selectAll(".node")
            .data(root.descendants())
            .enter().append("g")
            .attr("class", "node")
            .attr("transform", d => `translate(${d.x}, ${d.y})`);

        node.append("rect")
            .attr("width", 200)
            .attr("height", 80)
            .attr("x", -100)
            .attr("y", -40)
            .attr("stroke-width", "4px")
            .style("fill", d => {
                if (d.depth === 0) {
                    return "#FFFFE0"; // Root düğüm: açık sarı dolgu
                } else if (d.parent && d.parent.children[0] === d) {
                    return "#FFB6C1"; // Sol çocuk: pembe dolgu
                } else {
                    return "#B0E0E6"; // Sağ çocuk: açık mavi dolgu
                }
            })
            .style("stroke", d => {
                if (d.depth === 0) {
                    return "#FFD700"; // Root düğüm: sarı çerçeve
                } else if (d.parent && d.parent.children[0] === d) {
                    return "#FFC0CB"; // Sol çocuk: pembe çerçeve
                } else {
                    return "#ADD8E6"; // Sağ çocuk: mavi çerçeve
                }
            });

        node.append("text")
            .attr("text-anchor", "start")
            .attr("x", -95)
            .attr("y", -40)
            .selectAll("tspan")
            .data(d => [d.data.name, d.data.relation, `${d.data.birthDate}`, `${d.data.deathDate}`])
            .enter()
            .append("tspan")
            .attr("x", -95)
            .attr("dy", "1.2em")
            .text(d => d);
    })
    .catch(error => console.error('Error loading the data:', error));
});