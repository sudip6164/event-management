document.addEventListener("DOMContentLoaded", function() {
    const body = document.querySelector("body"),
          modeToggle = body.querySelector(".mode-toggle"),
          sidebar = body.querySelector("nav"),
          sidebarToggle = body.querySelector(".sidebar-toggle");
    
    // Check localStorage for mode preference
    let getMode = localStorage.getItem("mode");
    if (getMode && getMode === "dark") {
        body.classList.add("dark");
    }
    
    // Check localStorage for sidebar status
    let getStatus = localStorage.getItem("status");
    if (getStatus && getStatus === "close") {
        sidebar.classList.add("close");
        adjustDashboard();
    }
    
    // Dark mode toggle functionality
    if (modeToggle) {
        modeToggle.addEventListener("click", () => {
            body.classList.toggle("dark");
            localStorage.setItem("mode", body.classList.contains("dark") ? "dark" : "light");
        });
    }
    
    // Sidebar toggle functionality
    if (sidebarToggle) {
        sidebarToggle.addEventListener("click", () => {
            sidebar.classList.toggle("close");
            adjustDashboard();
            localStorage.setItem("status", sidebar.classList.contains("close") ? "close" : "open");
        });
    }
    
    // Function to adjust dashboard width and position
    function adjustDashboard() {
        const dashboard = document.querySelector(".dashboard");
        const dashboardTop = document.querySelector(".dashboard .top");
        
        if (sidebar.classList.contains("close")) {
            dashboard.style.left = "73px";
            dashboard.style.width = "calc(100% - 73px)";
            if (dashboardTop) {
                dashboardTop.style.left = "73px";
                dashboardTop.style.width = "calc(100% - 73px)";
            }
        } else {
            dashboard.style.left = "250px";
            dashboard.style.width = "calc(100% - 250px)";
            if (dashboardTop) {
                dashboardTop.style.left = "250px";
                dashboardTop.style.width = "calc(100% - 250px)";
            }
        }
    }
    
    // Initial adjustment
    adjustDashboard();
});

document.addEventListener("DOMContentLoaded", function() {
    // Pagination setup
    const tableRows = document.querySelectorAll("tbody tr");
    const rowsPerPage = 5;
    const totalPages = Math.ceil(tableRows.length / rowsPerPage);
    let currentPage = 1;
    
    // Create pagination container
    const paginationContainer = document.querySelector(".pagination");
    
    // Clear any existing pagination buttons
    paginationContainer.innerHTML = "";
    
    // Create pagination buttons
    const prevButton = document.createElement("button");
    prevButton.textContent = "Previous";
    prevButton.classList.add("btn", "btn-primary");
    prevButton.addEventListener("click", function() {
        if (currentPage > 1) {
            currentPage--;
            showPage(currentPage);
            updatePaginationButtons();
        }
    });
    paginationContainer.appendChild(prevButton);
    
    // Create page number buttons
    for (let i = 1; i <= totalPages; i++) {
        const pageButton = document.createElement("button");
        pageButton.textContent = i;
        pageButton.classList.add("btn", "btn-primary");
        pageButton.addEventListener("click", function() {
            currentPage = i;
            showPage(currentPage);
            updatePaginationButtons();
        });
        paginationContainer.appendChild(pageButton);
    }
    
    // Create next button
    const nextButton = document.createElement("button");
    nextButton.textContent = "Next";
    nextButton.classList.add("btn", "btn-primary");
    nextButton.addEventListener("click", function() {
        if (currentPage < totalPages) {
            currentPage++;
            showPage(currentPage);
            updatePaginationButtons();
        }
    });
    paginationContainer.appendChild(nextButton);
    
    // Function to show specific page
    function showPage(page) {
        const start = (page - 1) * rowsPerPage;
        const end = start + rowsPerPage;
        
        // Hide all rows
        tableRows.forEach(function(row, index) {
            if (index >= start && index < end) {
                row.style.display = "";
            } else {
                row.style.display = "none";
            }
        });
    }
    
    // Function to update active state of pagination buttons
    function updatePaginationButtons() {
        const pageButtons = paginationContainer.querySelectorAll(".btn");
        
        pageButtons.forEach(function(button, index) {
            // Skip first (prev) and last (next) buttons
            if (index === 0 || index === pageButtons.length - 1) {
                return;
            }
            
            // Check if this button represents the current page
            if (index === currentPage) {
                button.style.backgroundColor = "#4a56e2";
                button.style.color = "white";
            } else {
                button.style.backgroundColor = "";
                button.style.color = "";
            }
        });
        
        // Update prev/next button states
        prevButton.disabled = currentPage === 1;
        nextButton.disabled = currentPage === totalPages;
    }
    
    // Connect pagination with search functionality
    const searchInput = document.getElementById("searchInput");
    searchInput.addEventListener("keyup", function() {
        const searchTerm = searchInput.value.toLowerCase();
        let visibleRows = 0;
        
        tableRows.forEach(function(row) {
            const eventName = row.querySelector("td[data-label='Event Name']").innerText.toLowerCase();
            if (eventName.includes(searchTerm)) {
                row.classList.add("visible");
                visibleRows++;
            } else {
                row.classList.remove("visible");
            }
        });
        
        // Recalculate pagination based on visible rows
        const filteredRows = document.querySelectorAll("tbody tr.visible");
        const newTotalPages = Math.ceil(filteredRows.length / rowsPerPage);
        
        // Reset to first page when searching
        currentPage = 1;
        
        // Update pagination buttons
        updatePaginationForSearch(newTotalPages);
        
        // Show first page of filtered results
        showFilteredPage(1, filteredRows);
    });
    
    function updatePaginationForSearch(newTotalPages) {
        // Clear existing page number buttons
        const pageButtons = paginationContainer.querySelectorAll(".btn:not(:first-child):not(:last-child)");
        pageButtons.forEach(button => button.remove());
        
        // Create new page number buttons
        for (let i = 1; i <= newTotalPages; i++) {
            const pageButton = document.createElement("button");
            pageButton.textContent = i;
            pageButton.classList.add("btn", "btn-primary");
            pageButton.addEventListener("click", function() {
                currentPage = i;
                showFilteredPage(currentPage, document.querySelectorAll("tbody tr.visible"));
                updatePaginationButtons();
            });
            paginationContainer.insertBefore(pageButton, nextButton);
        }
        
        // If no pages, disable next button
        if (newTotalPages === 0) {
            prevButton.disabled = true;
            nextButton.disabled = true;
        } else {
            prevButton.disabled = currentPage === 1;
            nextButton.disabled = currentPage === newTotalPages;
        }
    }
    
    function showFilteredPage(page, filteredRows) {
        const start = (page - 1) * rowsPerPage;
        const end = start + rowsPerPage;
        
        // Hide all rows first
        tableRows.forEach(row => {
            row.style.display = "none";
        });
        
        // Show only filtered rows for current page
        filteredRows.forEach((row, index) => {
            if (index >= start && index < end) {
                row.style.display = "";
            }
        });
    }
    
    // Initialize: show first page and update buttons
    showPage(1);
    updatePaginationButtons();
});