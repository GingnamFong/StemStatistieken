console.log("hello world");

document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("onboarding-modal");
    const steps = modal.querySelectorAll(".step");
    let currentStep = 0;

    // Show modal if user hasn't completed onboarding
    if (!localStorage.getItem("onboardingComplete")) {
        modal.style.display = "flex";
    }

    modal.addEventListener("click", (e) => {
        if (e.target.classList.contains("next")) {
            steps[currentStep].style.display = "none";
            currentStep++;
            steps[currentStep].style.display = "block";
        } else if (e.target.classList.contains("finish")) {
            modal.style.display = "none";
            localStorage.setItem("onboardingComplete", "true");
        }
    });

    // Optional: Close modal by clicking outside content
    modal.addEventListener("click", (e) => {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});

// test comment