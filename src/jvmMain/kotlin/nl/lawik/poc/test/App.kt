package nl.lawik.poc.test

import nl.lawik.poc.test.endpoint.API_PATH
import javax.ws.rs.ApplicationPath
import javax.ws.rs.core.Application

@ApplicationPath(API_PATH)
class App : Application()