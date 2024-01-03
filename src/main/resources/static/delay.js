function delayRedirect() {
    // Set the delay in milliseconds (e.g., 3000ms for 3 seconds)
    const delay = 3000;

    // Replace 'second-url.html' with the actual URL you want to navigate to
    setTimeout(function () {
        window.location.href = 'result';
    }, delay);
}