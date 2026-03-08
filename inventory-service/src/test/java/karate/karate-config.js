function fn() {
  return {
    baseUrl: karate.properties['karate.baseUrl'],
    uuid: function() { return java.util.UUID.randomUUID() + ''; }
  };
}
