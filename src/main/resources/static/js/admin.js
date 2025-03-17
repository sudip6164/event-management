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