const $subscribe = document.querySelector('#subscribe');
const $unsubscribe = document.querySelector('#unsubscribe');
const $sendMessage = document.querySelector('#sendMessage');
const $message = document.querySelector('#message');
const $response = document.querySelector('#response');

$subscribe.addEventListener('click', async () => {

    toggleControlsState(true);

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
    });

    $response.textContent = 'Subscribed.';

    toggleControlsState(false);
});

$unsubscribe.addEventListener('click', async () => {

    toggleControlsState(true);

    const registration = await navigator.serviceWorker.ready;
    const subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        alert('No subscription');
        toggleControlsState(false);
        return;
    }

    await subscription.unsubscribe().then(successful => {
        console.log(successful);
        alert('Successfully unsubscribed.');
    }).catch(e => {
        console.error(e);
        alert('failed to unsubscribe.');
    });

    toggleControlsState(false);
});

$sendMessage.addEventListener('click', async () => {

    const message = $message.value;

    const result = await fetch('/sendMessage', {
        method: 'POST',
        body: JSON.stringify({ message }),
        headers: {
            'Content-Type': 'application/json'
        }
    }).then(response => response.text());

    $response.textContent = result;
});

function toggleControlsState(disabled) {
    $subscribe.disabled = disabled;
    $unsubscribe.disabled = disabled;
    $sendMessage.disabled = disabled;
    $message.disabled = disabled;
}