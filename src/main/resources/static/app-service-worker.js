self.addEventListener('push',
    event => event.waitUntil(handlePushEvent(event))
);

function handlePushEvent(event) {

    let msg = 'This is a notification body';
    if (event.data) {
        msg = event.data.text();
    }

    self.registration.showNotification('My Notification', {
        body: msg,
        tag: 'a tag'
    });
}
