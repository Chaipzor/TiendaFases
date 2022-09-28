package dam1.prog.tienda;

import java.util.ArrayList;
import java.util.Scanner;

import java.sql.*;

public class Tienda {
	public static void main(String[] args) {
		int[] datosUsuario = { 0, 0 }; // Primer dato: Tipo de usuario -- Segundo dato: id Usuario.
		// Tipos de usuarios: 0 Anónimo, 1 Cliente, 2 Empleado, 3 Administrador
		int primerMenu;
		int opcProd = 0;
		int elecProd = 0;
		int opcPerfil;
		String opcAdmin;
		String opcArt;
		String opcUser;
		String opcNormal = null;
		int opcCesta = 0;
		int idProd = 0;
		ArrayList<Producto> cesta = new ArrayList<Producto>();

		listarProductos("SELECT * FROM productos");
		// Repetiremos el menú hasta que el usuario quiera terminar.
		do {
			conectadoComo(datosUsuario[1]);
			primerMenu = menu(datosUsuario[0]);

			if (primerMenu == 0) {
				System.out.println("Fin. Esperamos volver a verte pronto!");
			}

			if (datosUsuario[0] == 0 || datosUsuario[0] == 1) { // Anónimo o cliente
				if (primerMenu == 1) { // Ver productos
					do {
						opcProd = menuProductos();
						if (opcProd == 1) {
							listarProductos("SELECT * FROM productos");
						} else if (opcProd == 2) {
							filtroPrecio();
						} else if (opcProd == 3) {
							filtroCategoria();
						} else if (opcProd == 4) {
							filtroComprados();
						}
					} while (opcProd != 0);

				} else if (primerMenu == 2) { // Ver detalles de producto
					idProd = detalleProducto();
					do {
						opcCesta = menuCesta();
						if (opcCesta == 1 && idProd != 0) {
							anadirCesta(cesta, idProd);
							verCesta(cesta);
						} else if (opcCesta != 0) {
							idProd = detalleProducto();
						}
					} while (opcCesta != 0);

				} else if (primerMenu == 5) { // Realizar pedido
					realizarPedido(datosUsuario, cesta);
				} else if (datosUsuario[0] == 0 && primerMenu == 3) { // Login
					datosUsuario = login();
				} else if (datosUsuario[0] == 0 && primerMenu == 4) { // Date de alta
					altaUsuario();
				} else if (datosUsuario[0] == 1 && primerMenu == 3) { // Ver historial de pedidos realizados
					listarPedidos(datosUsuario[1]);
				} else if (datosUsuario[0] == 1 && primerMenu == 4) { // Realizar pedido
					realizarPedido(datosUsuario, cesta);
				} else if (datosUsuario[0] == 1 && primerMenu == 5) { // Ver detalle del pedido
					listarPedidos(datosUsuario[1]);
					detallePedido();
				} else if (datosUsuario[0] == 1 && primerMenu == 6) { // Cancelar pedido
					listarPedidos(datosUsuario[1]);
					cancelarPedido(datosUsuario[1]);
				} else if (datosUsuario[0] == 1 && primerMenu == 7) { // Mi perfil
					do {
						verPerfil(datosUsuario[1]);
						opcPerfil = menuPerfil();
						edicionPerfil(datosUsuario[1], opcPerfil);
					} while (opcPerfil != 0);

				}

			} else if (datosUsuario[0] == 2 || datosUsuario[0] == 3) { // Empleado
				if (primerMenu == 1) { // Gestionar productos
					do {
						elecProd = gestionarProductos();
					} while (elecProd < 0 || elecProd > 2);
					if (elecProd == 1) {
						altaProducto();
					} else if (elecProd == 2) {
						// actualizarProducto();
					}
				} else if (primerMenu == 2) { // Gestionar clientes

				} else if (primerMenu == 3) { // Perfil de usuario
					do {
						verPerfil(datosUsuario[1]);
						opcPerfil = menuPerfil();
						edicionPerfil(datosUsuario[1], opcPerfil);
					} while (opcPerfil != 0);

				} else if (primerMenu == 4) { // Procesar pedidos

				} else if (datosUsuario[0] == 3 && primerMenu == 5) { // Administrador - Gestionar empleados

				} else if (datosUsuario[0] == 3 && primerMenu == 6) { // Procesar cancelaciones

				}
			}
		} while (primerMenu != 0);
		System.out.println("Hasta pronto!!");

	}

	private static void realizarPedido(int[] datosUsuario, ArrayList<Producto> cesta) {
		String numPedido = "";
		if (datosUsuario[0] == 0) {
			System.out.println("Debes darte de alta para finalizar la compra");
		} else {
			Statement instruccionSQL = conectarBBDD();
			try {
				if (cesta.size() > 0) {
					String query = "INSERT INTO pedidos (id_usuario, estado) values('" + datosUsuario[1] + "','" + "PE"
							+ "')";
					instruccionSQL.executeUpdate(query);

					query = "SELECT MAX(id) FROM pedidos";
					ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);

					while (resultadosConsulta.next()) {
						numPedido = resultadosConsulta.getString(1);
					}
					for (int i = 0; i < cesta.size(); i++) {
						query = "INSERT INTO detalles_pedido (id_pedido, id_producto, unidades , precio_unidad, impuesto) values('"
								+ numPedido + "','" + cesta.get(i).getId() + "','"
								+ cesta.get(i).getStock() + "','" + cesta.get(i).getPrecio() + "','" +
								cesta.get(i).getImpuesto() + "')";
						instruccionSQL.executeUpdate(query);
					}

				}
				cesta.clear();
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Error de conexión realizarPedido");
			}
		}
	}

	private static int menuCesta() {
		Scanner sc = new Scanner(System.in);

		System.out.println("1 - Añadir producto a la cesta");
		System.out.println("2 - Buscar otro producto");
		System.out.println("0 - Salir");

		int opcCesta = sc.nextInt();

		return opcCesta;
	}

	private static int menuProductos() {
		Scanner sc = new Scanner(System.in);
		System.out.println("1 - Ver todo el catálogo de productos");
		System.out.println("2 - Filtrar por precio");
		System.out.println("3 - Filtrar por categoría");
		System.out.println("4 - Top ventas");
		System.out.println("0 - Salir");
		int opcProd = sc.nextInt();
		return opcProd;
	}

	private static void conectadoComo(int idUsuario) {
		if (idUsuario == 0) {
			System.out.println("Conectado como: Anónimo");
		} else {
			try {
				Statement instruccionSQL = conectarBBDD();
				String query = "SELECT * FROM usuarios WHERE id = " + idUsuario;
				ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
				while (resultadosConsulta.next()) {
					System.out.print("Conectado como: ");
					System.out.print(resultadosConsulta.getString("nombre") + " - \t");
					System.out.println(resultadosConsulta.getString("email"));
				}
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Error de conexión conectadoComo");
			}
		}
	}

	public static int detalleProducto() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Selecciona el nº del producto a ver: ");
		int id = sc.nextInt();
		System.out.print("id");
		System.out.print("\t");
		System.out.print("nombre");
		System.out.print("\t");
		System.out.print("descripcion");
		System.out.print("\t");
		System.out.print("stock");
		System.out.print("\t");
		System.out.println("precio");

		try {
			Statement instruccionSQL = conectarBBDD();
			ResultSet resultadosConsulta = instruccionSQL.executeQuery("SELECT * FROM productos where id=" + id);
			while (resultadosConsulta.next()) {

				System.out.print(resultadosConsulta.getString("id"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("nombre"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("descripcion"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("stock"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("precio"));
				System.out.println("\n");
				return id;
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión detalleProducto");
			return 0;
		}
		return 0;
	}

	// Función para dar de alta los nuevos productos, calcula su precio de venta.
	public static void altaProducto() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Categoría de producto: ");
		int categoria = sc.nextInt();
		System.out.println("Nombre: ");
		String clr = sc.nextLine();
		String nombre = sc.nextLine();
		System.out.println("Descripción: ");
		String descripcion = sc.nextLine();
		System.out.println("Precio: ");
		int precio = sc.nextInt();
		System.out.println("Stock: ");
		int stock = sc.nextInt();
		Statement instruccionSQL = conectarBBDD();

		try {
			String query = "INSERT INTO productos (id_categoria, nombre, descripcion, precio, stock) values('"
					+ categoria + "','" + nombre + "','" + descripcion + "','" + precio + "','" + stock + "')";
			instruccionSQL.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión altaProducto");
		}

	}

	public static Statement conectarBBDD() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			java.sql.Connection conexion = DriverManager.getConnection("jdbc:mysql://localhost:3306/tiendafases",
					"root", "");
			Statement instruccionSQL = conexion.createStatement();
			return instruccionSQL;
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión");
		}
		return null;
	}

	public static boolean comprobarProducto(Statement instruccionSQL, String codigo) {
		try {
			ResultSet resultadosConsulta = instruccionSQL
					.executeQuery("SELECT id FROM productos WHERE id= '" + codigo + "'");
			if (resultadosConsulta.next()) {
				if (resultadosConsulta.getString("id").equals(codigo)) {
					System.out.println("Existe");
					return true;
				}
			} else {
				System.out.println("NO Existe");
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión comprobarProducto");
		}
		return false;
	}

	public static void actualizarProducto(Statement instruccionSQL, String codigo) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Cantidad: ");
		int nuevasUnidades = Integer.parseInt(sc.nextLine());
		System.out.println("Precio de compra: ");
		double precioCompra = Double.parseDouble(sc.nextLine());
		try {
			String query = "UPDATE productos SET stock = stock + '" + nuevasUnidades + "' WHERE id= '"
					+ codigo + "'";
			instruccionSQL.executeUpdate(query);
			query = "UPDATE productos SET precio_compra = '" + precioCompra + "' WHERE id= '" + codigo
					+ "'";
			instruccionSQL.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión comprobarProducto");
		}
	}

	public static void listarProductos(String query) {
		System.out.print("id");
		System.out.print("\t");
		System.out.print("nombre");
		System.out.print("\t");
		System.out.println("precio");

		try {
			Statement instruccionSQL = conectarBBDD();
			ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
			while (resultadosConsulta.next()) {

				System.out.print(resultadosConsulta.getString("id"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("nombre"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("precio"));
				System.out.println("\n");

			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión nuevoProducto");
		}
	}

	public static void listarCategorias(String query) {
		int contador = 0;
		try {
			Statement instruccionSQL = conectarBBDD();
			ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
			while (resultadosConsulta.next()) {
				contador++;
				System.out.print(contador + " - ");
				System.out.println(resultadosConsulta.getString("nombre"));

			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión nuevoProducto");
		}
	}

	public static void anadirCesta(ArrayList<Producto> cesta, int idProd) {
		Scanner sc = new Scanner(System.in);
		boolean aviso = false;
		int agregaUdsCesta = 0;
		try {
			agregaUdsCesta = cesta.get(idProd).getStock();
		} catch (Exception e) {

		}

		String codigo = String.valueOf(idProd);
		Statement instruccionSQL = conectarBBDD();
		boolean comprobacion = comprobarProducto(instruccionSQL, codigo); // 1 = Existe, 0 = No existe.

		if (comprobacion) {
			int codigoNumerico = Integer.parseInt(codigo);
			String query = "SELECT * FROM productos where id= '" + codigo + "'";

			try {
				ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
				while (resultadosConsulta.next()) {
					int id = resultadosConsulta.getInt("id");

					System.out.println("Introduce la cantidad a comprar: ");
					agregaUdsCesta = Integer.parseInt(sc.nextLine());
					int stock = resultadosConsulta.getInt("stock");
					if (agregaUdsCesta > stock) {
						System.out.println("Stock insuficiente. Cantidad máxima: " + stock);
					} else {
						for (int i = 0; i < cesta.size(); i++) {
							if (cesta.get(i).getId() == id) {
								int unidadesPrevias = cesta.get(i).getStock();
								agregaUdsCesta = agregaUdsCesta + unidadesPrevias;
								cesta.get(i).setStock(agregaUdsCesta);
								aviso = true;
							}
						}
						if (!aviso) {
							int id_categoria = resultadosConsulta.getInt("id_categoria");
							String nombre = resultadosConsulta.getString("nombre");
							String descripcion = resultadosConsulta.getString("descripcion");
							double precio_venta = resultadosConsulta.getDouble("precio");
							double impuesto = resultadosConsulta.getDouble("impuesto");
							Producto producto = new Producto(id, id_categoria, nombre, descripcion, precio_venta,
									agregaUdsCesta, impuesto);
							cesta.add(producto);
						}
					}

				}
			} catch (SQLException e) {
				System.err.println("Error al ejecutar la query (anadirCesta)");
			}
			try {
				instruccionSQL.close();
			} catch (SQLException e) {
				System.err.println("Error al cerrar la BBDD (anadirCesta)");
			}
		} else {
			System.out.println("Ese código de producto no existe.");
			try {
				instruccionSQL.close();
			} catch (SQLException e) {
				System.err.println("Error al cerrar la BBDD (anadirCesta)");
			}
		}
	}

	// Mostramos las opciones del menú principal.
	public static String menuUsuarios(int admin) {
		Scanner sc = new Scanner(System.in);

		System.out.println("\n\n1. Listar productos");
		System.out.println("2. Añadir a la cesta");
		System.out.println("3. Ver cesta");
		System.out.println("4. Eliminar de la cesta");
		System.out.println("5. Cupón descuento");
		if (admin == 1) {
			System.out.println("6. Volver al menú de administración");
			System.out.println("0. Ver ticket y volver al menú de administración");
		} else {
			System.out.println("0. Ver ticket y finalizar");
		}
		return sc.nextLine();
	}

	public static int menu(int opc) {
		Scanner sc = new Scanner(System.in);

		if (opc == 0) { // Menú anónimo
			System.out.println("1 - Ver productos");
			System.out.println("2 - Ver detalles de producto");
			System.out.println("3 - Login");
			System.out.println("4 - Date de alta");
			System.out.println("5 - Realizar pedido");

		} else if (opc == 1) { // Menú clientes registrados
			System.out.println("1 - Ver productos");
			System.out.println("2 - Ver detalles de producto");
			System.out.println("3 - Ver historial de pedidos realizados");
			System.out.println("4 - Realizar pedido");
			System.out.println("5 - Ver detalle del pedido");
			System.out.println("6 - Cancelar pedido");
			System.out.println("7 - Mi perfil");

		} else if (opc == 2 || opc == 3) { // Menú empleados
			System.out.println("1 - Gestionar productos");
			System.out.println("2 - Gestionar clientes");
			System.out.println("3 - Perfil de usuario");
			System.out.println("4 - Procesar pedidos");

		} else if (opc == 3) { // Menú administrador
			System.out.println("5 - Gestionar empleados");
			System.out.println("6 - Procesar cancelaciones");
		}

		System.out.println("0 - Salir");

		int eleccion = sc.nextInt();
		return eleccion;
	}

	public static int menuEmpleados(int opc) {
		Scanner sc = new Scanner(System.in);

		System.out.println("1 - Filtros");
		if (opc != 0) {
			System.out.println("2 - Perfil");
			System.out.println("3 - Cerrar sesión");
		} else {
			System.out.println("2 - Login");
			System.out.println("3 - Date de alta");
		}
		System.out.println("4 - Añadir a la cesta");
		System.out.println("0 - Salir");

		int eleccion = sc.nextInt();
		if (opc != 0) {
			eleccion = eleccion + 3; // 5 -> Perfil, 6 -> Cerrar sesión
		}
		return eleccion;
	}

	public static void filtroPrecio() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Introduce un mínimo");
		int minimo = sc.nextInt();
		System.out.println("Introduce un máximo");
		int maximo = sc.nextInt();
		System.out.println("\nListado de productos entre " + minimo + "€ y " + maximo + "€\n");
		listarProductos("SELECT * FROM productos WHERE precio BETWEEN " + minimo + " AND " + maximo);
	}

	public static void filtroCategoria() {
		Scanner sc = new Scanner(System.in);

		System.out.println("Selecciona la categoría a listar");
		listarCategorias("SELECT nombre FROM categorias");
		System.out.println("0 - Salir");

		int eleccion = sc.nextInt();

		listarProductos("SELECT * FROM productos WHERE id_categoria = " + eleccion);
	}

	public static int filtroComprados() {
		Scanner sc = new Scanner(System.in);

		System.out.println("0 - Salir");

		int eleccion = sc.nextInt();

		return eleccion;
	}

	// Comprobamos el login, si existe usuario y la clave.
	public static int[] login() {
		Scanner sc = new Scanner(System.in);
		String pass;
		int[] datosUsuario = { 0, 0 };
		System.out.println("Email de usuario: ");
		String user = sc.nextLine();
		try {
			// Class.forName("com.mysql.cj.jdbc.Driver");
			// java.sql.Connection conexion =
			// DriverManager.getConnection("jdbc:mysql://localhost:3306/tiendafases",
			// "root", "");
			// if (conexion != null) {
			// Statement instruccionSQL = conexion.createStatement();
			Statement instruccionSQL = conectarBBDD();
			ResultSet resultadosConsulta = instruccionSQL
					.executeQuery("SELECT * FROM usuarios WHERE email= '" + user + "'");
			System.out.println("Clave: ");
			pass = sc.nextLine();

			if (resultadosConsulta.next()) {
				boolean comprobacionUsuario = resultadosConsulta.getString("email").equals(user);
				boolean comprobacionPass = resultadosConsulta.getString("clave").equals(pass);
				if (comprobacionUsuario && comprobacionPass) {
					System.out.println("Bienvenido " + user);
					String idUsuario = resultadosConsulta.getString("id");
					String idRol = resultadosConsulta.getString("id_rol");
					datosUsuario[0] = Integer.parseInt(idRol);
					datosUsuario[1] = Integer.parseInt(idUsuario);
				} else {
					System.out.println("Usuario o contraseña inválidos.");
				}
			}
		} catch (

		Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión");
		}
		return datosUsuario;
	}

	// Se registra un nuevo usuario siempre y cuando no esté utilizado ya ese
	// nombre.
	public static void altaUsuario() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Email: ");
		String usuario = sc.nextLine();
		Statement instruccionSQL = conectarBBDD();
		boolean comprobacion = comprobarUsuario(instruccionSQL, usuario); // 1 = Ya existe, 0 = no existe.
		if (!comprobacion) {
			nuevoUsuario(instruccionSQL, usuario);
		} else {
			System.out.println("El email ya está siendo utilizado.");
		}
	}

	public static boolean comprobarUsuario(Statement instruccionSQL, String usuario) {
		try {
			ResultSet resultadosConsulta = instruccionSQL
					.executeQuery("SELECT email FROM usuarios WHERE email= '" + usuario + "'");
			if (resultadosConsulta.next()) {
				if (resultadosConsulta.getString("email").equals(usuario)) {
					return true;
				}
			} else {
				return false;
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión usuarios Usuario");
		}
		return false;
	}

	public static void nuevoUsuario(Statement instruccionSQL, String usuario) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Clave: ");
		String clave = sc.nextLine();
		System.out.println("Nombre: ");
		String nombre = sc.nextLine();
		System.out.println("Primer apellido: ");
		String primerApellido = sc.nextLine();
		System.out.println("Segundo apellido: ");
		String segundoApellido = sc.nextLine();
		System.out.println("Dirección: ");
		String direccion = sc.nextLine();
		System.out.println("Provincia: ");
		String provincia = sc.nextLine();
		System.out.println("Localidad: ");
		String localidad = sc.nextLine();
		System.out.println("Teléfono: ");
		String telefono = sc.nextLine();
		System.out.println("DNI: ");
		String dni = sc.nextLine();
		try {
			String query = "INSERT INTO usuarios (email, clave, nombre, apellido1, apellido2, direccion, provincia, localidad, telefono, dni) values('"
					+ usuario + "','" + clave + "','" + nombre + "','" + primerApellido + "','" + segundoApellido
					+ "','" + direccion + "','" + provincia + "','" + localidad + "','" + telefono + "','" + dni + "')";
			instruccionSQL.executeUpdate(query);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión nuevoUsuario");
		}
	}

	// Se da de baja el usuario introducido siempre que exista preaviemente
	public static void bajaUsuario() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Introduce el nombre de usuario: ");
		String usuario = sc.nextLine();
		Statement instruccionSQL = conectarBBDD();
		boolean comprobacion = comprobarUsuario(instruccionSQL, usuario); // 1 = Ya existe, 0 = no existe.
		if (comprobacion) {
			try {
				String query = "DELETE FROM usuarios where email='" + usuario + "'";
				instruccionSQL.executeUpdate(query);
				System.out.println("Usuario eliminado correctamente.");
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Error de conexión Borrar usuario");
			}
		}
	}

	public static void listarUsuarios() {
		try {
			Statement instruccionSQL = conectarBBDD();
			String query = "SELECT * FROM usuarios";
			ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
			while (resultadosConsulta.next()) {
				System.out.println(resultadosConsulta.getString("nombre"));
				System.out.println(resultadosConsulta.getString("email"));
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión listarUsuarios");
		}
	}

	public static void verPerfil(int datosUsuario) {
		Scanner sc = new Scanner(System.in);
		try {
			Statement instruccionSQL = conectarBBDD();
			String query = "SELECT * FROM usuarios WHERE id='" + datosUsuario + "'";
			ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
			while (resultadosConsulta.next()) {
				System.out.println("Dirección: " + resultadosConsulta.getString("email"));
				System.out.println("Nombre: " + resultadosConsulta.getString("nombre"));
				System.out.println("Primer apellido: " + resultadosConsulta.getString("apellido1"));
				System.out.println("Segundo apellido: " + resultadosConsulta.getString("apellido2"));
				System.out.println("Dirección: " + resultadosConsulta.getString("direccion"));
				System.out.println("Provincia: " + resultadosConsulta.getString("provincia"));
				System.out.println("Localidad: " + resultadosConsulta.getString("localidad"));
				System.out.println("Teléfono: " + resultadosConsulta.getString("telefono"));
				System.out.println("DNI: " + resultadosConsulta.getString("dni"));
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión verPerfil");
		}
	}

	public static int menuPerfil() {
		Scanner sc = new Scanner(System.in);
		System.out.println("\n1 - Modificar email");
		System.out.println("2 - Modificar nombre");
		System.out.println("3 - Modificar primer apellido");
		System.out.println("4 - Modificar segundo apellido");
		System.out.println("5 - Modificar dirección");
		System.out.println("6 - Modificar provincia");
		System.out.println("7 - Modificar teléfono");
		System.out.println("8 - Modificar dni");
		System.out.println("0 - Salir");
		return sc.nextInt();
	}

	public static void edicionPerfil(int datosUsuario, int opcion) {
		Scanner sc = new Scanner(System.in);
		Statement instruccionSQL = conectarBBDD();
		String variable = null;
		int nuevoTelefono = -1;
		String nuevoDato = null;
		if (opcion != 7 || opcion != 0) {
			if (opcion == 1) {
				System.out.println("\n1 - Introduce email");
				variable = "email";
			} else if (opcion == 2) {
				System.out.println("2 - Introduce nombre");
				variable = "nombre";
			} else if (opcion == 3) {
				System.out.println("3 - Introduce primer apellido");
				variable = "apellido1";
			} else if (opcion == 4) {
				System.out.println("4 - Introduce segundo apellido");
				variable = "apellido2";
			} else if (opcion == 5) {
				System.out.println("5 - Introduce dirección");
				variable = "direccion";
			} else if (opcion == 6) {
				System.out.println("6 - Introduce provincia");
				variable = "provincia";
			} else if (opcion == 8) {
				System.out.println("8 - Introduce dni");
				variable = "dni";
			}
			nuevoDato = sc.nextLine();
			try {
				String query = "UPDATE usuarios SET " + variable + " = '" + nuevoDato + "' WHERE id= '"
						+ datosUsuario + "'";
				instruccionSQL.executeUpdate(query);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Error de conexión editarUsuario");
			}
		}
		if (opcion == 7) {
			System.out.println("7 - Introduce teléfono");
			nuevoTelefono = sc.nextInt();
			try {
				String query = "UPDATE usuarios SET telefono = '" + nuevoTelefono + "' WHERE id= '"
						+ datosUsuario + "'";
				instruccionSQL.executeUpdate(query);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Error de conexión editarUsuario");
			}
		}
	}

	// Menú para el administrador, con la posibilidad de desbloquear (o resetear a 0
	// los intentos)
	public static String panelAdmin() {
		Scanner sc = new Scanner(System.in);
		System.out.println("\n1) Control de artículos");
		System.out.println("2) Control de usuarios");
		System.out.println("3) Panel de clientes");
		System.out.println("0) Salir");
		return sc.nextLine();
	}

	public static String panelArticulos() {
		Scanner sc = new Scanner(System.in);
		System.out.println("\n1) Alta");
		System.out.println("2) Listado");
		System.out.println("0) Salir");
		return sc.nextLine();
	}

	public static String panelUsuarios() {
		Scanner sc = new Scanner(System.in);
		System.out.println("\n1) Alta");
		System.out.println("2) Baja");
		System.out.println("3) Listar");
		System.out.println("4) Editar");
		System.out.println("5) Desbloquear");
		System.out.println("6) Desbloquear todos");
		System.out.println("0) Salir");
		return sc.nextLine();
	}

	// Muestra la cesta

	public static void verCesta(ArrayList<Producto> cesta) {
		System.out.println("\tCESTA:");
		if (cesta.size() == 0) {
			System.out.println("Tu cesta está vacía.");
		} else {
			System.out.println("Listado de productos y cantidades: ");
			for (int i = 0; i < cesta.size(); i++) {
				System.out.println(cesta.get(i).toString());
			}
		}
	}

	public static void listarPedidos(int id_usuario) {
		System.out.print("ID Pedido: ");
		System.out.print("\t");
		System.out.print("ID Usuario: ");
		System.out.print("\t");
		System.out.print("Fecha: ");
		System.out.print("\t");
		System.out.print("Método de pago: ");
		System.out.print("\t");
		System.out.print("Estado: ");
		System.out.print("\t");
		System.out.print("Nº Factura: ");
		System.out.print("\t");
		System.out.print("Total: ");

		try {
			Statement instruccionSQL = conectarBBDD();
			String query = "SELECT * FROM pedidos WHERE id_usuario = '" + id_usuario + "'";
			ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
			while (resultadosConsulta.next()) {
				System.out.print(resultadosConsulta.getString("id"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("id_usuario"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("fecha"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("metodo_pago"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("estado"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("num_factura"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("total"));
			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión listarPedidos");
		}
	}

	public static void detallePedido() {
		Scanner sc = new Scanner(System.in);
		System.out.println("Introduce el número de pedido para ver sus detalles: ");
		int id_pedido = sc.nextInt();
		System.out.print("ID Pedido: ");
		System.out.print("\t");
		System.out.print("ID Producto: ");
		System.out.print("\t");
		System.out.print("Unidades: ");
		System.out.print("\t");
		System.out.print("Precio: ");
		System.out.print("\t");
		System.out.print("Impuesto: ");
		System.out.print("\t");
		System.out.print("Total: ");

		try {
			Statement instruccionSQL = conectarBBDD();
			String query = "SELECT * FROM detalles_pedido WHERE id_pedido ='" + id_pedido + "'";
			ResultSet resultadosConsulta = instruccionSQL.executeQuery(query);
			while (resultadosConsulta.next()) {
				System.out.print(resultadosConsulta.getString("id_pedido"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("id_producto"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("unidades"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("precio"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("impuesto"));
				System.out.print("\t");
				System.out.print(resultadosConsulta.getString("total"));

			}
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión detallePedido");
		}
	}

	public static void cancelarPedido(int usuario) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Introduce el número de pedido a cancelar: ");
		int id_pedido = sc.nextInt();
		Statement instruccionSQL = conectarBBDD();
		try {
			String query = "DELETE FROM pedidos where id_usuario='" + usuario + "' AND id = '" + id_pedido + "'";
			instruccionSQL.executeUpdate(query);
			System.out.println("Pedido eliminado correctamente.");
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("Error de conexión eliminar pedido");
		}
	}

	public static int gestionarProductos() {
		Scanner sc = new Scanner(System.in);
		System.out.println("1 - Alta nuevo producto");
		System.out.println("2 - Ajuste stock");
		System.out.println("0 - Salir");
		return sc.nextInt();
	}

}