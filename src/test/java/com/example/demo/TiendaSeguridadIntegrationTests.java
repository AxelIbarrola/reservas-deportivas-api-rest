package com.example.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:tiendatests;DB_CLOSE_DELAY=-1",
    "demo.admin.password=clave-admin-pruebas",
    "demo.admin.username=administrador-test"
})
@AutoConfigureMockMvc
class TiendaSeguridadIntegrationTests {
    @Autowired MockMvc mvc;

    @Test
    void permisosRolesComprasPropiasStockYCors() throws Exception {
        String usuario1 = "cliente1-" + UUID.randomUUID();
        String usuario2 = "cliente2-" + UUID.randomUUID();
        registrar(usuario1);
        registrar(usuario2);
        String jwt1 = login(usuario1, "contrasena123");
        String jwt2 = login(usuario2, "contrasena123");
        String admin = login("administrador-test", "clave-admin-pruebas");

        // Catálogo público; el usuario sin token no puede crear productos.
        mvc.perform(get("/productos")).andExpect(status().isOk());
        mvc.perform(post("/productos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Mouse\",\"precio\":12000,\"stock\":3}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/productos").header("Authorization", bearer(jwt1))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Mouse\",\"precio\":12000,\"stock\":3}"))
            .andExpect(status().isForbidden());

        String producto = mvc.perform(post("/productos").header("Authorization", bearer(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nombre\":\"Mouse\",\"precio\":12000,\"stock\":3}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long idProducto = ((Number) JsonPath.read(producto, "$.idProducto")).longValue();
        mvc.perform(put("/productos/" + idProducto).header("Authorization", bearer(jwt1))
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/productos/" + idProducto).header("Authorization", bearer(jwt1)))
            .andExpect(status().isForbidden());
        String perfil = mvc.perform(get("/clientes/me").header("Authorization", bearer(jwt1)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long idCliente = ((Number) JsonPath.read(perfil, "$.idCliente")).longValue();
        assertEquals("SIN_DESCUENTO", JsonPath.read(perfil, "$.tipoDescuento"));
        assertEquals(0, ((Number) JsonPath.read(perfil, "$.comprasRealizadas")).intValue());
        mvc.perform(get("/clientes").header("Authorization", bearer(jwt1)))
            .andExpect(status().isForbidden());
        mvc.perform(get("/clientes").header("Authorization", bearer(admin)))
            .andExpect(status().isOk());

        String rutaDescuento = "/clientes/" + idCliente + "/descuento";
        String beneficio = "{\"tipoDescuento\":\"CLIENTE_PREFERENCIAL\"}";
        // Nadie puede asignarse descuentos: el endpoint exige ROLE_ADMIN.
        mvc.perform(put(rutaDescuento).contentType(MediaType.APPLICATION_JSON).content(beneficio))
            .andExpect(status().isUnauthorized());
        mvc.perform(put(rutaDescuento).header("Authorization", bearer(jwt1))
            .contentType(MediaType.APPLICATION_JSON).content(beneficio))
            .andExpect(status().isForbidden());
        String resultadoAsignacion = mvc.perform(put(rutaDescuento).header("Authorization", bearer(admin))
            .contentType(MediaType.APPLICATION_JSON).content(beneficio))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(10, ((Number) JsonPath.read(resultadoAsignacion, "$.porcentajeDescuento")).intValue());
        mvc.perform(put(rutaDescuento).header("Authorization", bearer(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"tipoDescuento\":null}"))
            .andExpect(status().isBadRequest());

        // POST /facturas usa el cliente asociado al JWT; jamás el id enviado por USER.
        String factura = mvc.perform(post("/facturas").header("Authorization", bearer(jwt1)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long idFactura = ((Number) JsonPath.read(factura, "$.idFactura")).longValue();
        String perfilConBorrador = mvc.perform(get("/clientes/me")
            .header("Authorization", bearer(jwt1)))
            .andReturn().getResponse().getContentAsString();
        assertEquals(0, ((Number) JsonPath.read(perfilConBorrador, "$.comprasRealizadas")).intValue());
        mvc.perform(post("/facturas?idCliente=1").header("Authorization", bearer(jwt1)))
            .andExpect(status().isForbidden());
        mvc.perform(get("/facturas/" + idFactura).header("Authorization", bearer(jwt2)))
            .andExpect(status().isForbidden());
        mvc.perform(post("/facturas/" + idFactura + "/emitir?tipo=SIN_DESCUENTO")
            .header("Authorization", bearer(jwt2))).andExpect(status().isForbidden());
        mvc.perform(get("/facturas").header("Authorization", bearer(jwt1)))
            .andExpect(status().isForbidden());
        mvc.perform(get("/facturas/mias").header("Authorization", bearer(jwt1)))
            .andExpect(status().isOk());
        mvc.perform(get("/facturas").header("Authorization", bearer(admin)))
            .andExpect(status().isOk());

        mvc.perform(post("/facturas/" + idFactura + "/detalles?idProducto=" + idProducto + "&cantidad=2")
            .header("Authorization", bearer(jwt1))).andExpect(status().isCreated());
        // Aun agregando parámetros inventados, el descuento viene de Cliente, no del navegador.
        String emitida = mvc.perform(post("/facturas/" + idFactura + "/emitir?tipo=PORCENTAJE&porcentaje=99")
            .header("Authorization", bearer(jwt1)))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("EMITIDA", JsonPath.read(emitida, "$.estado"));
        assertEquals(21600.0, ((Number) JsonPath.read(emitida, "$.total")).doubleValue(), 0.01);
        assertEquals(2400.0, ((Number) JsonPath.read(emitida, "$.descuentoAplicado")).doubleValue(), 0.01);
        assertEquals("CLIENTE_PREFERENCIAL", JsonPath.read(emitida, "$.tipoDescuento"));
        assertEquals(10, ((Number) JsonPath.read(emitida, "$.porcentajeDescuento")).intValue());

        String perfilConCompra = mvc.perform(get("/clientes/me").header("Authorization", bearer(jwt1)))
            .andReturn().getResponse().getContentAsString();
        assertEquals(1, ((Number) JsonPath.read(perfilConCompra, "$.comprasRealizadas")).intValue());

        // Al cambiar la categoría NO se recalcula la factura histórica.
        mvc.perform(put(rutaDescuento).header("Authorization", bearer(admin))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"tipoDescuento\":\"CLIENTE_VIP\"}"))
            .andExpect(status().isOk());
        String comprobanteViejo = mvc.perform(get("/facturas/" + idFactura)
            .header("Authorization", bearer(jwt1))).andReturn().getResponse().getContentAsString();
        assertEquals(10, ((Number) JsonPath.read(comprobanteViejo, "$.porcentajeDescuento")).intValue());
        assertEquals(21600.0, ((Number) JsonPath.read(comprobanteViejo, "$.total")).doubleValue(), 0.01);

        String otraFactura = mvc.perform(post("/facturas").header("Authorization", bearer(jwt1)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long idOtra = ((Number) JsonPath.read(otraFactura, "$.idFactura")).longValue();
        mvc.perform(post("/facturas/" + idOtra + "/detalles?idProducto=" + idProducto + "&cantidad=1")
            .header("Authorization", bearer(jwt1))).andExpect(status().isCreated());
        String facturaVip = mvc.perform(post("/facturas/" + idOtra + "/emitir")
            .header("Authorization", bearer(jwt1))).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        assertEquals("CLIENTE_VIP", JsonPath.read(facturaVip, "$.tipoDescuento"));
        assertEquals(10200.0, ((Number) JsonPath.read(facturaVip, "$.total")).doubleValue(), 0.01);
        String perfilDosCompras = mvc.perform(get("/clientes/me").header("Authorization", bearer(jwt1)))
            .andReturn().getResponse().getContentAsString();
        assertEquals(2, ((Number) JsonPath.read(perfilDosCompras, "$.comprasRealizadas")).intValue());
        String nuevoStock = mvc.perform(get("/productos/" + idProducto))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(0, ((Number) JsonPath.read(nuevoStock, "$.stock")).intValue());

        // Preflight CORS: origen permitido y encabezado Authorization.
        mvc.perform(options(rutaDescuento)
            .header("Origin", "http://127.0.0.1:5500")
            .header("Access-Control-Request-Method", "PUT")
            .header("Access-Control-Request-Headers", "authorization,content-type"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5500"));
        mvc.perform(options("/facturas/mias")
            .header("Origin", "http://127.0.0.1:5500")
            .header("Access-Control-Request-Method", "GET")
            .header("Access-Control-Request-Headers", "authorization"))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://127.0.0.1:5500"));
    }

    private String bearer(String token) { return "Bearer " + token; }
    private void registrar(String usuario) throws Exception {
        String datos = """
          {"username":"%s","password":"contrasena123","nombre":"Ana","apellido":"Test","email":"%s@ejemplo.com"}
          """.formatted(usuario, usuario);
        mvc.perform(post("/api/auth/signup").contentType(MediaType.APPLICATION_JSON).content(datos))
            .andExpect(status().isCreated());
    }
    private String login(String usuario, String clave) throws Exception {
        String datos = """ 
          {"username":"%s","password":"%s"}
          """.formatted(usuario, clave);
        String cuerpo = mvc.perform(post("/api/auth/signin").contentType(MediaType.APPLICATION_JSON)
            .content(datos)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return JsonPath.read(cuerpo, "$.token");
    }
}
