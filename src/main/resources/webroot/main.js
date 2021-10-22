const $subscribe = document.querySelector('#subscribe');
const $unsubscribe = document.querySelector('#unsubscribe');
const $sendMessage = document.querySelector('#sendMessage');
const $message = document.querySelector('#message');
const $response = document.querySelector('#response');
const $progress = document.querySelector('#progress');

$subscribe.addEventListener('click', async () => {

    startProcessing();

    const serverPublicKey = await fetch('/getPublicKey')
                                    .then(response => response.arrayBuffer());

    await navigator.serviceWorker.register('/app-service-worker.js');

    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: serverPublicKey
    });

    const subscriptionStr = JSON.stringify(subscription);

    await fetch('/subscribe', {
        method: 'POST',
        body: subscriptionStr,
        headers: {
            'content-type': 'application/json'
        }
    }).then(res => {
        if (res.ok) {
            setMessage('Subscribed.', true);
        } else {
            setMessage('Something wrong happened when sending the subscription to the application server.', false);
        }
    });


    endProcessing();
});

$unsubscribe.addEventListener('click', async () => {

    startProcessing();

    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        setMessage('No subscription', false);
        endProcessing();
        return;
    }

    await subscription.unsubscribe().then(successful => {
        console.log(successful);
        setMessage('Successfully unsubscribed.', true);
    }).catch(e => {
        console.error(e);
        setMessage('Failed to unsubscribe.', false);
    });

    endProcessing();
});

$sendMessage.addEventListener('click', async () => {

    const message = $message.value;

    await fetch('/sendMessage', {
        method: 'POST',
        body: JSON.stringify({ message }),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(async res => {
        if (res.ok) {
            const message = await res.text()
            setMessage(message, true);
        } else {
            setMessage('Failed to send the message.', false);
        }
    });

});
endProcessing();

/* Utility functions */

function setMessage(text, isSuccess) {
    $response.textContent = text;
    const classes = [ 'has-text-info', 'has-text-warning' ];
    classes.forEach(cls => $response.classList.remove(cls));
    if (isSuccess) {
        $response.classList.add('has-text-info');
    } else {
        $response.classList.add('has-text-warning');
    }
}

function startProcessing() {
    toggleControlsState(true);
    changeProgressState(true);
}

function endProcessing() {
    toggleControlsState(false);
    changeProgressState(false);
}

function changeProgressState(isProcessed) {
    if (isProcessed) {
        $progress.style.display = 'block';
        $response.style.display = 'none';
    } else {
        $progress.style.display = 'none';
        $response.style.display = 'block';
    }
}

function toggleControlsState(disabled) {
    $subscribe.disabled = disabled;
    $unsubscribe.disabled = disabled;
    $sendMessage.disabled = disabled;
    $message.disabled = disabled;
}